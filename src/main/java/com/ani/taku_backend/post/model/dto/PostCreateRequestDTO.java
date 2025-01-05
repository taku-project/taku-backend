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
public class PostCreateRequestDTO {

    @Schema(description = "카테고리Id")
    private Long categoryId;

    @Schema(description = "제목")
    @Size(max = 150, message = "제목은 최대 150글자까지 입력 가능합니다.")
    private String title;

    @Schema(description = "내용")
    @Size(max = 150, message = "내용은 최대 3000글자까지 입력 가능합니다.")
    private String content;
}
