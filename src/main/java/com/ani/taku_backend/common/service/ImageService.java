package com.ani.taku_backend.common.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.FILE_MAX_REGIST_EXCEED;
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

        // 이미지 업로드
        List<String> imageUrlList = uploadProductImageList(imageList);

        List<Image> saveImageList = new ArrayList<>();

        for (int i = 0; i < imageUrlList.size(); i++) {
            MultipartFile imageFile = imageList.get(i);
            String imageUrl = imageUrlList.get(i);
            // 이미지 파일 이름 추출(UUID)
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // 원래 이미지 파일 이름 추출
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = extractFileExtension(originalFilename);  // 파일 확장자 추출

            Image image = Image.builder()
                    .user(user)
                    .fileName(fileName)
                    .imageUrl(imageUrl)
                    .originalName(originalFilename)
                    .fileType(fileExtension)
                    .fileSize((int) imageFile.getSize())
                    .build();

            saveImageList.add(imageRepository.save(image));
            log.info("이미지 저장 성공 {}", image);
        }

        return saveImageList;

    }

    @Transactional
    public List<Image> getUpdateImageList(ProductUpdateRequestDTO productUpdateRequestDTO, List<MultipartFile> newImageList, DuckuJangter findProduct, User user) {

        // 게시글에서 첨부파일을 모두 삭제하고 넘어옴
        if (newImageList == null || newImageList.isEmpty()) {
            findProduct.getJangterImages().forEach(communityImage -> {
                Image image = communityImage.getImage();
                fileService.deleteImageFile(image.getFileName());    // s3 에서 삭제(클라우드 플레어)
                image.delete();                                 // RDB에서 삭제
                log.info("저장할 이미지 없음 -> 이미지 삭제 성공");
            });
        }

        // 삭제 대상인 이미지 리스트 -> productUpdateRequestDTO.getDeleteImageUrl();
        List<String> deleteImageUrl = productUpdateRequestDTO.getDeleteImageUrl();
        if (deleteImageUrl != null && !deleteImageUrl.isEmpty()) {
            deleteImageUrl.forEach(imageUrl -> {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                fileService.deleteImageFile(filename);  // s3 에서 삭제(클라우드 플레어)
            });
            imageRepository.findByFileNameIn(deleteImageUrl).forEach(Image::delete);    // RDB 삭제
            log.info("삭제 대상 이미지 삭제 성공 {}", deleteImageUrl);
        }

        // 저장할 이미지 -> newImageList
        List<Image> addImageList = null;
        if (newImageList != null) {
            addImageList = saveImageList(newImageList, user);
        }
        return addImageList;
    }

    // 이미지 5개 검증 후 이미지를 업로드하는 메서드
    @Transactional
    protected List<String> uploadProductImageList(List<MultipartFile> imageList) {
        List<String> imageUrlList = new ArrayList<>();
        for (MultipartFile image : imageList) {
            try {
                validateImageCount(imageList);    // 5개 이상이면 예외 발생
                String imageUrl = fileService.uploadImageFile(image);
                log.info("r2 이미지 파일 업로드 성공 {}", imageUrl);
                imageUrlList.add(imageUrl);
            } catch (IOException e) {
                throw new DuckwhoException(FILE_UPLOAD_ERROR);
            }
        }
        return imageUrlList;
    }

    // 이미지 파일 확장자 추출 메서드
    private String extractFileExtension(String originalFilename) {
        String fileExtension = null;
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            log.info("확장자 추출 성공 {}", fileExtension);
        }
        return fileExtension;
    }

    // 이미지 5개 이상 저장 불가
    private void validateImageCount(List<MultipartFile> imageList) {
        if (imageList != null && imageList.size() > 5) {
            throw new DuckwhoException(FILE_MAX_REGIST_EXCEED);
        }
    }

}
