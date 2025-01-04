package com.ani.taku_backend.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final AmazonS3 client;

    @Value("${cloud.flare.video-public.url}")
    private String videoPublicUrl;

    @Value("${cloud.flare.video-bucket}")
    private String videoBucket;

    @Value("${cloud.flare.image-public.url}")
    private String imagePublicUrl;

    @Value("${cloud.flare.image-bucket}")
    private String imageBucket;

    public String uploadVideoFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());  // Content-Type 설정

        // ACL을 public-read로 설정
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                videoBucket,
                fileName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);  // public-read ACL 추가

        client.putObject(putObjectRequest);
        return videoPublicUrl + "/" + fileName;
    }

    public String uploadImageFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        log.info("{} 입니다. {} 입니다.", imageBucket, imagePublicUrl);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());  // Content-Type 설정
        // ACL을 public-read로 설정
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                imageBucket,
                fileName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);  // public-read ACL 추가

        client.putObject(putObjectRequest);
        return imagePublicUrl + "/" + fileName;
    }

    private String generateFileName(String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        return UUID.randomUUID() + "." + fileExtension;
    }

    public S3Object getVideoFile(String fileName) throws AmazonS3Exception {
        try {
            return client.getObject(new GetObjectRequest(videoBucket, fileName));
        } catch (Exception e) {
            throw new AmazonS3Exception("Failed to retrieve file from S3: " + e.getMessage(), e);
        }
    }

    public S3Object getImageFile(String fileName) throws AmazonS3Exception {
        try {
            return client.getObject(new GetObjectRequest(imageBucket, fileName));
        } catch (Exception e) {
            throw new AmazonS3Exception("Failed to retrieve file from S3: " + e.getMessage(), e);
        }
    }


    // 이미지 삭제
    public void deleteImageFile(String fileName) {
        try {
            client.deleteObject(imageBucket, fileName);
            log.info("이미지 삭제 완료 imageBucket: {}, fileName: {}",imageBucket, fileName);
        } catch (AmazonS3Exception e) {
            throw new AmazonS3Exception("Failed to delete file from Cloudflare R2: " + e.getMessage(), e);
        }
    }

    // 비디오 삭제
    public void deleteVideoFile(String fileName) {
        try {
            client.deleteObject(videoBucket, fileName);
            log.info("비디오 삭제 완료 videoBucket: {}, fileName: {}",videoBucket, fileName);
        } catch (AmazonS3Exception e) {
            throw new AmazonS3Exception("Failed to delete file from Cloudflare R2: " + e.getMessage(), e);
        }
    }
}
