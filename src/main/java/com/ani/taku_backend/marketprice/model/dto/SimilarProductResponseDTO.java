package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유사 상품 정보 DTO")
public class SimilarProductResponseDTO {
    @Schema(description = "상품 ID", example = "123")
    private final Long productId;

    @Schema(description = "상품 제목", example = "원피스 루피 피규어")
    private final String title;

    @Schema(description = "등록 가격", example = "21000")
    private final BigDecimal price;

    @Schema(description = "유사도 점수", example = "0.85")
    private final double similarity;

    @Schema(description = "상품 이미지 URL 목록")
    private final List<String> imageUrls;

    public static SimilarProductResponseDTO from(TfidfService.ProductWithSimilarity productWithSimilarity) {
        DuckuJangter product = productWithSimilarity.getProduct();

        return SimilarProductResponseDTO.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .similarity(productWithSimilarity.getSimilarity())
                .imageUrls(product.getJangterImages().stream()
                        .map(image -> image.getImage().getImageUrl())
                        .collect(Collectors.toList()))
                .build();
    }

    public static SimilarProductResponseDTO from(DuckuJangter product, double similarity) {
        return SimilarProductResponseDTO.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .similarity(similarity)
                .imageUrls(product.getJangterImages().stream()
                        .map(image -> image.getImage().getImageUrl())
                        .collect(Collectors.toList()))
                .build();
    }
}