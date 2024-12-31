package com.ani.taku_backend.shorts.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsInfoResDTO;
import com.ani.taku_backend.shorts.service.ShortsService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
@Validated
@Tag(name = "쇼츠 API", description = "파일 API")
@Slf4j
public class ShortsController {

    private final FileService fileUploadService;
    private final ShortsService shortsService;
    @Operation(summary = "파일 업로드", description = "파일을 스토리지에 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File upload : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFile(@AuthenticationPrincipal PrincipalUser userDetails, @Valid @ModelAttribute ShortsCreateReqDTO shortsCreateReqDTO) {
        User user = userDetails.getUser();
        shortsService.createShort(shortsCreateReqDTO, user);
        return "파일이 스토리지에 업로드 되었습니다. UploadUrl: ";
    }

    @Operation(summary = "파일 다운로드", description = "파일을 스토리지에서 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File download : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws AmazonS3Exception {
        try {
            S3Object s3Object = fileUploadService.getFile(fileName);
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

    @Operation(
        summary = "쇼츠 추천", description = "쇼츠를 추천합니다.",
        security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts recommend : SUCCESS")
    })
    @GetMapping("/recommend")
    public com.ani.taku_backend.common.response.ApiResponse<List<ShortsInfoResDTO>> getRecommendShorts(
        @AuthenticationPrincipal PrincipalUser userDetails
    ) {
        return com.ani.taku_backend.common.response.ApiResponse.ok(this.shortsService.findRecommendShorts(userDetails));
    }

    @Operation(summary = "쇼츠 댓글 조회", description = "쇼츠 댓글을 조회합니다.",
        security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment : SUCCESS")
    })
    @GetMapping("/comment")
    public com.ani.taku_backend.common.response.ApiResponse<List<ShortsCommentDTO>> findShortsComment(
        @Parameter(description = "쇼츠 아이디", required = true) @RequestParam(value = "shortsId", required = true) String shortsId) {
        log.info("쇼츠 댓글 조회 요청: shortsId = {}", shortsId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return com.ani.taku_backend.common.response.ApiResponse.ok(shortsComment);
    }
    

}