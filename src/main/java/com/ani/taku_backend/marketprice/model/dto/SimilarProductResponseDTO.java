package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.common.util.TypeIdResolverForDevTools;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonTypeIdResolver(TypeIdResolverForDevTools.class)
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
    private static String extractImageUrl(DuckuJangter product) {
        return product.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);
    }

    private static SimilarProductResponseDTO createFrom(DuckuJangter product, String imageUrl) {
        return new SimilarProductResponseDTO(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getTfidfVector(),
                imageUrl
        );
    }

    public static SimilarProductResponseDTO from(DuckuJangter product) {
        return createFrom(product, extractImageUrl(product));
    }

    public static SimilarProductResponseDTO from(TfidfService.ProductWithSimilarity productWithSimilarity) {
        DuckuJangter product = productWithSimilarity.getProduct();
        return createFrom(product, extractImageUrl(product));
    }
}