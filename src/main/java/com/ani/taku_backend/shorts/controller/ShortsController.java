package com.ani.taku_backend.shorts.controller;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentUpdateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsInfoResDTO;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsResponseDTO;
import com.ani.taku_backend.shorts.domain.entity.VideoType;
import com.ani.taku_backend.shorts.service.ShortsService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Shorts API", description = "Shorts 관련 API")
@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
@Validated
@Tag(name = "쇼츠 API", description = "파일 API")
@Slf4j
public class ShortsController {
    private final ShortsService shortsService;
    private final BlackUserService blackUserService;
    @Operation(summary = "쇼츠 업로드", description = "파일을 스토리지에 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API 요청 성공"),
            @ApiResponse(responseCode = "400", description = "입력한 값의 유효성이 올바르지 않을 때"),
            @ApiResponse(responseCode = "403", description = "사용자 로그인이 되어있지 않았을 때")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResponse<String> uploadFile(@AuthenticationPrincipal PrincipalUser userDetails,
        @Valid @ModelAttribute ShortsCreateReqDTO shortsCreateReqDTO) {

        MultipartFile file = shortsCreateReqDTO.getFile();
        // 파일 확장자 동영상 파일인지 검토
        if(VideoType.isSupportedFileFormat(file.getName())) {
            throw new FileException(ErrorCode.INVALID_FILE_FORMAT);
        }
        User user = userDetails.getUser();
        validateBlackUser(user.getUserId());

        shortsService.createShort(shortsCreateReqDTO, user);

        return CommonResponse.created("성공적으로 등록되었습니다.");
    }

    @Operation(summary = "Shorts 상세 정보 조회", description = "쇼츠 동영상에 필요한 정보 반환")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 정보 불러오기에 성공했습니다."),
        @ApiResponse(responseCode = "404", description = "파일 정보를 찾을 수 없습니다.")
    })
    @GetMapping("/{shortsId}")
    public CommonResponse<ShortsResponseDTO> findShortsInfo(
            @AuthenticationPrincipal PrincipalUser userDetails,
            @Parameter(description = "조회할 쇼츠의 고유 ID", required = true)
            @PathVariable("shortsId") String shortsId
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        ShortsResponseDTO shortsResponseDTO = shortsService.findShortsInfo(shortsId, userId);
        return CommonResponse.ok(shortsResponseDTO);
    }

    @Operation(
        summary = "쇼츠 추천", description = "쇼츠를 추천합니다.",
        security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts recommend : SUCCESS")
    })
    @GetMapping("/recommend")
    public CommonResponse<List<ShortsInfoResDTO>> getRecommendShorts(
        @AuthenticationPrincipal PrincipalUser userDetails
    ) {
        return CommonResponse.ok(this.shortsService.findRecommendShorts(userDetails));
    }

    @Operation(summary = "쇼츠 댓글 조회", description = "쇼츠 댓글을 조회합니다.",
        security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment : SUCCESS")
    })
    @GetMapping("/{shortsId}/comment")
    public CommonResponse<List<ShortsCommentDTO>> findShortsComment(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId) {
        log.info("쇼츠 댓글 조회 요청: shortsId = {}", shortsId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.ok(shortsComment);
    }

    @Operation(summary = "쇼츠 댓글 생성", description = "쇼츠 댓글을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment : SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Not Found: Shorts not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Shorts save error.")
    })
    @PostMapping("/{shortsId}/comment")
     public CommonResponse<List<ShortsCommentDTO>> createShortsComment(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId,
        @Parameter(description = "댓글 내용", required = true) @Valid @RequestBody ShortsCommentCreateReqDTO shortsCommentCreateReqDTO
     ) {

        // 댓글 생성
        this.shortsService.createShortsComment(null, shortsCommentCreateReqDTO , shortsId);

        // 댓글 조회
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.created(shortsComment);
     }

     /**
      * 댓글 수정

      * @param shortsCommentUpdateReqDTO
      */
    @Operation(summary = "쇼츠 댓글 수정", description = "쇼츠 댓글을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment : SUCCESS"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated."),
            @ApiResponse(responseCode = "404", description = "Not Found: Shorts comment not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Shorts comment update error.")
    })
    @PatchMapping("/{shortsId}/comment/{commentId}")
     public CommonResponse<List<ShortsCommentDTO>>  updateShortsComment(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId,
        @Parameter(description = "댓글 아이디", required = true) @PathVariable(value = "commentId") String commentId,
        @Parameter(description = "댓글 내용", required = true) @Valid @RequestBody ShortsCommentUpdateReqDTO shortsCommentUpdateReqDTO
     ) {
        this.shortsService.updateShortsComment(null, shortsCommentUpdateReqDTO, commentId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.ok(shortsComment);
     }

     /**
      * 댓글삭제
      * @param shortsId
      * @param commentId
      */
    @Operation(summary = "쇼츠 댓글 삭제", description = "쇼츠 댓글을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment : SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Not Found: Shorts comment not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Shorts comment delete error.")
    })
    @DeleteMapping("/{shortsId}/comment/{commentId}")
    public CommonResponse<List<ShortsCommentDTO>> deleteShortsComment(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId,
        @Parameter(description = "댓글 아이디", required = true) @PathVariable(value = "commentId") String commentId
    ) {
        this.shortsService.deleteShortsComment(null, commentId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.ok(shortsComment);
    }


    /**
     * 대댓글 생성
     * @param shortsId
     * @param commentId
     * @param shortsCommentCreateReqDTO
     */
    @Operation(summary = "쇼츠 댓글 대댓글 생성", description = "쇼츠 댓글 대댓글을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment reply : SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Not Found: Shorts comment not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Shorts comment reply save error.")
    })
    @PostMapping("/{shortsId}/comment/{commentId}/reply")
    public CommonResponse<List<ShortsCommentDTO>> createShortsReply(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId,
        @Parameter(description = "댓글 아이디", required = true) @PathVariable(value = "commentId") String commentId,
        @Parameter(description = "대댓글 내용", required = true) @Valid @RequestBody ShortsCommentCreateReqDTO shortsCommentCreateReqDTO
    ) {
        this.shortsService.createShortsReply(null, shortsCommentCreateReqDTO, commentId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.ok(shortsComment);
    }

    /**
     * 대댓글 삭제
     * @param shortsId
     * @param commentId
     * @param replyId
     */
    @Operation(summary = "쇼츠 댓글 대댓글 삭제", description = "쇼츠 댓글 대댓글을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shorts comment reply : SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Not Found: Shorts comment reply not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Shorts comment reply delete error.")
    })
    @DeleteMapping("/{shortsId}/comment/{commentId}/reply/{replyId}")
    public CommonResponse<List<ShortsCommentDTO>> deleteShortsReply(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId,
        @Parameter(description = "댓글 아이디", required = true) @PathVariable(value = "commentId") String commentId,
        @Parameter(description = "대댓글 아이디", required = true) @PathVariable(value = "replyId") String replyId
    ) {
        this.shortsService.deleteShortsReply(null, commentId, replyId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.ok(shortsComment);
    }

    /**
     * 대댓글 수정
     * @param shortsId
     * @param commentId
     * @param replyId
     */
    @Operation(summary = "쇼츠 댓글 대댓글 수정", description = "쇼츠 댓글 대댓글을 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Shorts comment reply : SUCCESS"),
        @ApiResponse(responseCode = "404", description = "Not Found: Shorts comment reply not found."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: Shorts comment reply update error.")
    })
    @PatchMapping("/{shortsId}/comment/{commentId}/reply/{replyId}")
    public CommonResponse<List<ShortsCommentDTO>> updateShortsReply(
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable(value = "shortsId") String shortsId,
        @Parameter(description = "댓글 아이디", required = true) @PathVariable(value = "commentId") String commentId,
        @Parameter(description = "대댓글 아이디", required = true) @PathVariable(value = "replyId") String replyId,
        @Parameter(description = "대댓글 내용", required = true) @Valid @RequestBody ShortsCommentUpdateReqDTO shortsCommentUpdateReqDTO
    ) {
        this.shortsService.updateShortsReply(null, shortsCommentUpdateReqDTO, commentId, replyId);
        List<ShortsCommentDTO> shortsComment = this.shortsService.findShortsComment(shortsId);
        return CommonResponse.ok(shortsComment);
    }

    private void validateBlackUser(Long userId) {
        List<BlackUser> blackUser = blackUserService.findByUserId(userId);
        if(!blackUser.isEmpty()) {
            throw new DuckwhoException(ErrorCode.BLACK_USER);
        }
    }
}
