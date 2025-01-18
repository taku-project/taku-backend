package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Schema(description = "유사 상품 정보 DTO")
public record SimilarProductResponseDTO(
        @Schema(description = "상품 ID", example = "123")
        Long productId,

        @Schema(description = "상품 제목", example = "원피스 루피 피규어")
        String title,

        @Schema(description = "등록 가격", example = "21000")
        BigDecimal price,

        @Schema(description = "TF-IDF 벡터 값", example = "0.5,0.3,0.2")
        String tfidfVector,

        @Schema(description = "대표 썸네일 URL", example = "https://example.com/image.jpg")
        String imageUrl
) {
    public static SimilarProductResponseDTO from(DuckuJangter product) {
        String singleImageUrl = product.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);

        return new SimilarProductResponseDTO(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getTfidfVector(),
                singleImageUrl
        );
    }


    public static SimilarProductResponseDTO from(TfidfService.ProductWithSimilarity productWithSimilarity) {
        DuckuJangter product = productWithSimilarity.getProduct();
        String singleImageUrl = product.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);

        return new SimilarProductResponseDTO(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getTfidfVector(),
                singleImageUrl
        );
    }
}