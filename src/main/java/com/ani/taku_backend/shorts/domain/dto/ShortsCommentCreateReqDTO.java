package com.ani.taku_backend.shorts.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쇼츠 댓글 생성 요청 DTO")
public class ShortsCommentCreateReqDTO {

    @Schema(description = "쇼츠 댓글 내용")
    private String comment;

    @Schema(description = "쇼츠 아이디")
    @JsonProperty("shorts_id")
    private String shortsId;
}
