package com.ani.taku_backend.jangter.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 상품 및 관련 정보를 한 번에 조회하기 위한 DTO
 */
@Getter
public class ProductAggregateDTO {
    private final DuckuJangter product;
    private final String articleImage;
    private final String articleTitle;
    private final BigDecimal articlePrice;
    private final Long sellerId;

    public ProductAggregateDTO(
            DuckuJangter product,
            String articleImage,
            String articleTitle,
            BigDecimal articlePrice,
            Long sellerId) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        this.articleImage = articleImage;
        this.articleTitle = articleTitle;
        this.articlePrice = articlePrice;
        this.sellerId = sellerId;
    }
} 