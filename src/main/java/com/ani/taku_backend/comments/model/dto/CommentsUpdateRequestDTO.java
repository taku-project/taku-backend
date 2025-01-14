package com.ani.taku_backend.comments.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentsUpdateRequestDTO {

    @Schema(description = "게시글 ID", example = "17")
    @NotNull(message = "{NotNull.comment.postId}")
    private Long postId;

    @Schema(description = "댓글 내용", example = "편하게 테스트 댓글 달기")
    @NotNull(message = "{NotNull.comment.content}")
    @Size(max = 255, message = "댓글은 255자 이하로 작성해 주세요.")
    private String content;

}
