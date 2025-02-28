package com.ani.taku_backend.common.remote_file;

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
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloudflareR2VideoFileService implements RemoteFileService {
    private final AmazonS3 client;

    @Value("${cloud.flare.video-public.url}")
    private String videoPublicUrl;

    @Value("${cloud.flare.video-bucket}")
    private String videoBucket;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = this.generateFileName(Objects.requireNonNull(file.getOriginalFilename()));
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
        } catch (IOException e) {
            throw new DuckwhoException(ErrorCode.FILE_ERROR);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String filePath) {
        try {
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
        } catch (IOException e) {
            throw new DuckwhoException(ErrorCode.FILE_ERROR);
        }
    }

    @Override
    public S3Object getFile(String fileName) {
        try {
            return client.getObject(new GetObjectRequest(videoBucket, fileName));
        } catch (Exception e) {
            throw new AmazonS3Exception("Failed to retrieve file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDirectory(String path) {
        try {
            // folderPath 하위의 객체 리스트 가져오기
            ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                    .withBucketName(videoBucket)
                    .withPrefix(path);

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
            throw new DuckwhoException(ErrorCode.FILE_DELETE_FAIL);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            client.deleteObject(videoBucket, fileName);
        } catch (AmazonS3Exception e) {
            throw new AmazonS3Exception("Failed to delete file from Cloudflare R2: " + e.getMessage(), e);
        }
    }
}
