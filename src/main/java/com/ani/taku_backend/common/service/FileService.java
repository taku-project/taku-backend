package com.ani.taku_backend.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public String uploadVideoFile(MultipartFile file, String filePath) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());  // Content-Type 설정

        // ACL을 public-read로 설정
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                videoBucket,
                filePath,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);  // public-read ACL 추가

        client.putObject(putObjectRequest);

        return videoPublicUrl + "/" + filePath;
    }

    public String uploadImageFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        log.info("imageBucket: {}, imagePublicUrl: {}", imageBucket, imagePublicUrl);
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

    public void deleteFolder(String folderPath) {
        try {
            // folderPath 하위의 객체 리스트 가져오기
            ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                    .withBucketName(videoBucket)
                    .withPrefix(folderPath);

            ListObjectsV2Result result = client.listObjectsV2(listObjectsRequest);

            // 가져온 객체를 삭제 요청 리스트로 변환
            List<DeleteObjectsRequest.KeyVersion> keysToDelete = result.getObjectSummaries().stream()
                    .map(file -> new DeleteObjectsRequest.KeyVersion(file.getKey()))
                    .collect(Collectors.toList());

            // 삭제 요청
            if (!keysToDelete.isEmpty()) {
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(videoBucket)
                        .withKeys(keysToDelete);
                client.deleteObjects(deleteObjectsRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("폴더 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 이미지 삭제
    public void deleteImageFile(String fileName) {
        try {
            client.deleteObject(imageBucket, fileName);
            log.debug("이미지 삭제 완료 imageBucket: {}, fileName: {}",imageBucket, fileName);
        } catch (AmazonS3Exception e) {
            throw new AmazonS3Exception("Failed to delete file from Cloudflare R2: " + e.getMessage(), e);
        }
    }

}
