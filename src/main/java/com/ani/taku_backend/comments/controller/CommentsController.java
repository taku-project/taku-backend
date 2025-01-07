package com.ani.taku_backend.comments.controller;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.dto.CommentsUpdateRequestDTO;
import com.ani.taku_backend.comments.service.CommentsService;
import com.ani.taku_backend.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/comments")
@Slf4j
public class CommentsController {

    private final CommentsService commentsService;

    /**
     * 댓글 생성
     */
    @Operation(
            summary = "커뮤니티 댓글 작성",
            description = "커뮤니티 댓글 쓰기, 부모아이디가 null이면 댓글, 부모아이디가 있으면 해당 댓글의 대댓글")
    @ApiResponses({
            @ApiResponse(responseCode = "201",description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 댓글")
    })
    @PostMapping
    public CommonResponse<Long> createComments(
            @Parameter(
                    description = "판매글 생성 요청 JSON 데이터", required = true
            )
            @Valid @RequestBody CommentsCreateRequestDTO commentsCreateRequestDTO) {

        Long saveCommentsId = commentsService.createComments(commentsCreateRequestDTO, null);
        return CommonResponse.created(saveCommentsId);
    }

    /**
     * 댓글 수정
     */
    @Operation(
            summary = "커뮤니티 댓글 수정",
            description = "커뮤니티 댓글 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 댓글")
    })
    @PutMapping("/{commentsId}")
    public CommonResponse<Long> updateComments(
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentsId") long commentsId,
            @Parameter(
                    description = "댓글 수정 요청 JSON 데이터", required = true
            )
            @Valid @RequestBody CommentsUpdateRequestDTO commentsUpdateRequestDTO) {

        Long updateCommentsId = commentsService.updateComments(commentsId, commentsUpdateRequestDTO, null);
        return CommonResponse.ok(updateCommentsId);
    }

    /**
     * 댓글 삭제
     */
    @Operation(
            summary = "커뮤니티 댓글 삭제",
            description = "커뮤니티 댓글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 댓글")
    })
    @DeleteMapping("/{commentsId}")
    public CommonResponse<Void> deleteComments(
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentsId") long commentsId) {

        commentsService.deleteComments( commentsId, null);
        return CommonResponse.ok(null);
    }
}
