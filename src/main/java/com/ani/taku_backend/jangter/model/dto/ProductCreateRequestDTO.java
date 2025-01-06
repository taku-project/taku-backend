package com.ani.taku_backend.jangter.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "덕후 장터 게시글 생성 요청 DTO")
public class ProductCreateRequestDTO {

    @Schema(description = "카테고리 ID")
    @NotNull(message = "{NotNull.categoryId}")
    private Long categoryId;

    @Schema(description = "판매글 제목")
    @NotNull(message = "{NotNull.title}")
    @Size(min = 5, max = 150, message = "제목은 5자 이상, 150자 이하로 입력해 주세요.")
    private String title;

    @Schema(description = "판매글 본문")
    @NotNull(message = "{NotNull.description}")
    @Size(min = 10, max = 3000, message = "본문은 10자 이상, 3000자 이하로 입력해 주세요.")
    private String description;

    @Schema(description = "가격")
    @NotNull(message = "{NotNull.price}")
    @DecimalMin(value = "0.01", message = "가격은 0.01 이상이어야 합니다.")
    @DecimalMax(value = "10000000.00", message = "가격은 10,000,000 이하이어야 합니다.")
    private BigDecimal price;

    public ProductCreateRequestDTO(Long categoryId, String title, String description, BigDecimal price) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
    }
}
