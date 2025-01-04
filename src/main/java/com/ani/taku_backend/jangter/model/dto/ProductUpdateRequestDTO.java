package com.ani.taku_backend.jangter.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "게시글 업데이트 요청 DTO")
public class ProductUpdateRequestDTO {

    @Schema(description = "카테고리 ID")
    private Long categoryId;

    @Schema(description = "판매글 제목")
    private String title;

    @Schema(description = "판매글 내용")
    private String description;

    @Schema(description = "가격")
    private BigDecimal price;

    @Schema(description = "삭제할 이미지 URL 리스트")
    private List<String> deleteImageUrl;

    public ProductUpdateRequestDTO(Long categoryId, String title, String description, BigDecimal price, List<String> deleteImageUrl) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.deleteImageUrl = deleteImageUrl;
    }
}
