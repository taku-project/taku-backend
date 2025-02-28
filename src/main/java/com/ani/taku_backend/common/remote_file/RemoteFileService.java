package com.ani.taku_backend.common.remote_file;

import com.amazonaws.services.s3.model.S3Object;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface RemoteFileService {
    String uploadFile(MultipartFile file);
    String uploadFile(MultipartFile file, String filePath);

    S3Object getFile(String fileName);

    void deleteDirectory(String path);

    void deleteFile(String fileName);

    default String generateFileName(String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        return UUID.randomUUID() + "." + fileExtension;
    }
}
