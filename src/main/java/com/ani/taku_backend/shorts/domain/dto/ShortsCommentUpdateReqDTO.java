package com.ani.taku_backend.shorts.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "쇼츠 댓글 수정 요청 DTO")
public class ShortsCommentUpdateReqDTO {
    
    @Schema(description = "쇼츠 댓글 내용")
    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    @JsonProperty("comment")
    private String comment;
}
