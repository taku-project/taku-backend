package com.ani.taku_backend.comments.model.dto;

import com.ani.taku_backend.common.enums.StatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentsCreateRequestDTO {

    @Schema(description = "게시글 ID", example = "17")
    @NotNull(message = "{NotNull.comment.postId}")
    private Long postId;

    @Schema(description = "댓글 내용", example = "편하게 테스트 댓글 달기")
    @NotNull(message = "{NotNull.comment.content}")
    @Size(max = 255, message = "댓글은 255자 이하로 작성해 주세요.")
    private String content;

    @Schema(description = "부모 댓글 ID (null: 댓글, 댓글Id: 해당 댓글Id의 대댓글)")
    private Long parentCommentId;

}
