package com.ani.taku_backend.common.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.stereotype.Service;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.FILE_UPLOAD_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileService fileService;


    public Image insertImage(Image image) {
        return this.imageRepository.save(image);
    }


    @Transactional
    public List<Image> saveImageList(List<MultipartFile> imageList, User user) {

        List<Image> saveImageList = new ArrayList<>();

        // 이미지 업로드
        List<String> imageUrlList = uploadProductImageList(imageList);

        for (String imageUrl : imageUrlList) {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            for (MultipartFile getImage : imageList) {
                String originalFilename = getImage.getOriginalFilename();
                String fileExtension = null;
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                    log.info("확장자 추출 성공 {}", fileExtension);
                }

                Image image = Image.builder()
                        .user(user)
                        .fileName(fileName)
                        .imageUrl(imageUrl)
                        .originalName(originalFilename)
                        .fileType(fileExtension)
                        .fileSize((int) getImage.getSize())
                        .build();

                saveImageList.add(imageRepository.save(image));
                log.info("이미지 저장 성공 {}", image);
            }
        }
        return saveImageList;
    }

    // 이미지 5개 검증 후 이미지를 업로드
    @Transactional
    protected List<String> uploadProductImageList(List<MultipartFile> imageList) {
        List<String> imageUrlList = new ArrayList<>();
        for (MultipartFile image : imageList) {
            try {
                validateImageCount(imageList);    // 5개 이상이면 예외 발생
                String imageUrl = fileService.uploadFile(image);
                log.info("r2 이미지 파일 업로드 성공 {}", imageUrl);
                imageUrlList.add(imageUrl);
            } catch (IOException e) {
                throw new DuckwhoException(FILE_UPLOAD_ERROR);
            }
        }
        return imageUrlList;
    }

    // 이미지 5개 이상 저장 불가
    private void validateImageCount(List<MultipartFile> imageList) {
        if (imageList != null && imageList.size() > 5) {
            throw new FileException.FileUploadException("5개 이상 이미지를 등록할 수 없습니다.");
        }
    }

}
