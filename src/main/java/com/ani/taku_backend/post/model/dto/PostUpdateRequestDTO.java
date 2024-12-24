package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.dto.ImageCreateRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDTO {

    @Schema(description = "카테고리 Id")
    private Long categoryId;

    @Size(max = 50, message = "제목은 최대 50글자까지 입력 가능합니다.")
    @Schema(description = "제목")
    private String title;

    @Schema(description = "내용")
    private String content;

    @Schema(description = "첨부파일 이미지")
    private List<ImageCreateRequestDTO> imagelist;
}
