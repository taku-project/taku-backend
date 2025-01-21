package com.ani.taku_backend.comments.controller;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.dto.CommentsUpdateRequestDTO;
import com.ani.taku_backend.comments.service.CommentsService;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/comments")
@Slf4j
public class CommentsController {

    private final CommentsService commentsService;
    private final BlackUserService blackUserService;

    /**
     * 댓글 생성
     */
    @Operation(summary = "커뮤니티 댓글 작성",
              description = "커뮤니티 댓글 쓰기, 부모아이디가 null이면 댓글, 부모아이디가 있으면 부모아이디의 대댓글")
    @ApiResponses({
            @ApiResponse(responseCode = "201",description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 댓글")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUser
    public CommonResponse<Long> createComments(@Valid CommentsCreateRequestDTO commentsCreateRequestDTO,
                                               @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);       // Black 유저 검증
        Long saveCommentsId = commentsService.createComments(commentsCreateRequestDTO, user);
        return CommonResponse.created(saveCommentsId);
    }

    /**
     * 댓글 수정
     */
    @Operation(summary = "커뮤니티 댓글 수정", description = "커뮤니티 댓글 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 댓글")
    })
    @PutMapping(path = "/{commentsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUser
    public CommonResponse<Long> updateComments(
            @Parameter(description = "댓글 ID(구글 토큰 입력", required = true, example = "6") @PathVariable("commentsId") long commentsId,
            @Valid CommentsUpdateRequestDTO commentsUpdateRequestDTO,
            @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);         // Black 유저 검증
        Long updateCommentsId = commentsService.updateComments(commentsId, commentsUpdateRequestDTO, user);
        return CommonResponse.ok(updateCommentsId);
    }

    /**
     * 댓글 삭제
     */
    @Operation(summary = "커뮤니티 댓글 삭제", description = "커뮤니티 댓글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 댓글")
    })
    @DeleteMapping("/{commentsId}")
    public CommonResponse<Void> deleteComments(
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentsId") long commentsId,
            @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);         // Black 유저 검증
        commentsService.deleteComments(commentsId, user);
        return CommonResponse.ok(null);
    }
}
