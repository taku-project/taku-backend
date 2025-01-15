package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
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

    @Schema(description = "TF-IDF 벡터 값", example = "0.5,0.3,0.2")
    private final String tfidfVector;

    @Schema(description = "대표 썸네일 URL", example = "https://example.com/image.jpg")
    private final String imageUrl;

    @Builder
    public SimilarProductResponseDTO(
            Long productId,
            String title,
            BigDecimal price,
            String tfidfVector,
            String imageUrl
    ) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.tfidfVector = tfidfVector;
        this.imageUrl = imageUrl;
    }

    public static SimilarProductResponseDTO from(DuckuJangter product) {
        String singleImageUrl = product.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);

        return SimilarProductResponseDTO.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .tfidfVector(product.getTfidfVector())
                .imageUrl(singleImageUrl)
                .build();
    }

    public static SimilarProductResponseDTO from(TfidfService.ProductWithSimilarity productWithSimilarity) {
        DuckuJangter product = productWithSimilarity.getProduct();
        String singleImageUrl = product.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);

        return SimilarProductResponseDTO.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .tfidfVector(product.getTfidfVector())
                .imageUrl(singleImageUrl)
                .build();
    }
}