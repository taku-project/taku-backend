package com.ani.taku_backend.shorts.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.ani.taku_backend.common.response.ApiResponse;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsInfoResDTO;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsResponseDTO;
import com.ani.taku_backend.shorts.service.ShortsService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
//            @ApiResponse(responseCode = "200", description = "File upload : SUCCESS"),
//            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFile(@AuthenticationPrincipal PrincipalUser userDetails, @Valid @ModelAttribute ShortsCreateReqDTO shortsCreateReqDTO) {
        User user = userDetails.getUser();
        shortsService.createShort(shortsCreateReqDTO, user);
        return "파일이 스토리지에 업로드 되었습니다. UploadUrl: ";
    }

    @Operation(summary = "m3u8 PlayList url 반환", description = "m3u8 PlayList url 반환")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File download : SUCCESS"),
//            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
//    })
    @GetMapping("/{shortsId}")
    public ApiResponse<ShortsResponseDTO> findM3u8Url(@PathVariable(name = "shortsId") String shortsId, Model model) throws AmazonS3Exception {
        ShortsResponseDTO shortsResponseDTO = shortsService.findShortsInfo(shortsId);
        return ApiResponse.ok(shortsResponseDTO);
    }

    @PostMapping("/{shortsId}/like")
    public void shortsLike(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @PathVariable(name = "shortsId") String shortsId) {
        User user = principalUser.getUser();
        shortsService.shortsLike(user, shortsId);
    }
//    @GetMapping("/{shortsId}")
//    public ApiResponse<String> findM3u8Url(@PathVariable String shortsId) throws AmazonS3Exception {
//        String m3u8FileURL = shortsService.findM3u8FileURL(shortsId);
//
//        return ApiResponse.ok(m3u8FileURL);
//    }


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

    // TODO: 댓글 추가
    /**
     * /comment POST
     * 로그인 유저만 가능
     * header: Authorization: Bearer {token}
     * 
     * JSON
     * {
     *  "comment": "string",
     *  "shorts_id": "string"
     * }
     */

     @PostMapping("/comment")
     public com.ani.taku_backend.common.response.ApiResponse<List<ShortsCommentDTO>> createShortsComment(
        @RequestBody ShortsCommentCreateReqDTO shortsCommentCreateReqDTO
     ) {

        // 댓글 생성
        this.shortsService.createShortsComment(null, shortsCommentCreateReqDTO);
        
        // 댓글 조회
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsCommentCreateReqDTO.getShortsId());
        return com.ani.taku_backend.common.response.ApiResponse.created(shortsComment);
     }

    // TODO: 댓글 수정
    /**
     * /comment/{commentId} PUT
     * 로그인 유저만 가능
     * header: Authorization: Bearer {token}
     * 
     * JSON
     * {
     *  "shorts_id": "string",
     *  "comment": "string",
     *  "comment_id": "string"
     * }
     */

    // TODO: 댓글 삭제


    // TODO: 댓글 대댓글 추가

    // TODO: 댓글 대댓글 삭제

    // TODO: 댓글 대댓글 수정
}
