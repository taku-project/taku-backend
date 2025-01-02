package com.ani.taku_backend.common.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
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

    @Transactional
    public List<Image> getUpdateImageList(ProductUpdateRequestDTO productUpdateRequestDTO, List<MultipartFile> newImageList, DuckuJangter findProduct, User user) {

        // 게시글에서 첨부파일을 모두 삭제하고 넘어옴
        if (newImageList == null || newImageList.isEmpty()) {
            findProduct.getJangterImages().forEach(communityImage -> {
                Image image = communityImage.getImage();
                fileService.deleteFile(image.getFileName());    // s3 에서 삭제(클라우드 플레어)
                image.delete();                                 // RDB에서 삭제
            });
        }

        // 삭제 대상인 이미지 리스트 -> productUpdateRequestDTO.getDeleteImageUrl();
        List<String> deleteImageUrl = productUpdateRequestDTO.getDeleteImageUrl();
        if (deleteImageUrl != null && !deleteImageUrl.isEmpty()) {
            deleteImageUrl.forEach(imageUrl -> {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                fileService.deleteFile(filename);  // s3 에서 삭제(클라우드 플레어)
            });
            imageRepository.findByFileNameIn(deleteImageUrl).forEach(Image::delete);    // RDB 삭제
        }

        // 저장할 이미지 -> newImageList
        List<Image> addImageList = null;
        if (newImageList != null) {
            addImageList = saveImageList(newImageList, user);
        }

        return addImageList;
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
