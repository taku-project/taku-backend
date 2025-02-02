package com.ani.taku_backend.common.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.ani.taku_backend.common.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Log4j2
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "파일 API", description = "파일 API")
public class FileController {

    private final FileService fileUploadService;

    @Operation(summary = "비디오 업로드", description = "비디오를 스토리지에 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video upload : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @PostMapping(path = "/upload/video", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadVideoFile(@RequestParam("video") MultipartFile file) throws IOException {
        String uploadUrl = fileUploadService.uploadVideoFile(file);
        return "비디오가 스토리지에 업로드 되었습니다. UploadUrl: " + uploadUrl;
    }

    @Operation(summary = "비디오 다운로드", description = "비디오을 스토리지에서 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video download : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @GetMapping("/download/video/{fileName}")
    public ResponseEntity<Resource> downloadVideoFile(@PathVariable String fileName) throws AmazonS3Exception {
        try {
            S3Object s3Object = fileUploadService.getVideoFile(fileName);
            byte[] fileContent = s3Object.getObjectContent().readAllBytes();
            Resource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new AmazonS3Exception("Failed to download file: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "이미지 업로드", description = "이미지를 스토리지에 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File upload : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @PostMapping(path = "/upload/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadImageFile(@RequestParam("file") MultipartFile file) throws IOException {
        String uploadUrl = fileUploadService.uploadImageFile(file);
        return "이미지가 스토리지에 업로드 되었습니다. UploadUrl: " + uploadUrl;
    }

    @Operation(summary = "이미지 다운로드", description = "이미지를 스토리지에서 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File download : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @GetMapping("/download/image/{fileName}")
    public ResponseEntity<Resource> downloadImageFile(@PathVariable String fileName) throws AmazonS3Exception {
        try {
            S3Object s3Object = fileUploadService.getImageFile(fileName);
            byte[] fileContent = s3Object.getObjectContent().readAllBytes();
            Resource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new AmazonS3Exception("Failed to download file: " + e.getMessage(), e);
        }
    }
}
