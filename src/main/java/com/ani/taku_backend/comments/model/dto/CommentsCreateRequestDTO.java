package com.ani.taku_backend.comments.model.dto;

import com.ani.taku_backend.common.enums.StatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "커뮤니티 게시글 댓글 생성 DTO")
public class CommentsCreateRequestDTO {

    @Schema(description = "게시글 ID")
    @NotNull(message = "{NotNull.comment.postId}")
    private Long postId;

    @Schema(description = "댓글 내용")
    @NotNull(message = "{NotNull.comment.content}")
    @Size(min = 0, max = 255, message = "댓글은 255자 이하로 작성해 주세요.")
    private String content;

    @Schema(description = "부모 댓글 ID (null일 경우 댓글, 값이 있는 경우 해당 댓글의 대댓글)")
    private Long parentCommentId;

    public CommentsCreateRequestDTO(long postId, String content, Long parentCommentId) {
        this.postId = postId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }
}