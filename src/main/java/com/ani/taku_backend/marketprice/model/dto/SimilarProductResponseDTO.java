package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

/**
 * 유사 상품 정보 DTO
 */
@Getter
@Builder
@JsonDeserialize(builder = SimilarProductResponseDTO.SimilarProductResponseDTOBuilder.class)
@Schema(description = "유사 상품 정보 DTO")
public class SimilarProductResponseDTO {

    @Schema(description = "상품 ID", example = "123")
    private final Long productId;

    @Schema(description = "상품 제목", example = "원피스 루피 피규어")
    private final String title;

    @Schema(description = "등록 가격", example = "21000")
    private final BigDecimal price;

    @Schema(description = "TF-IDF 벡터 값(예시)", example = "JSON 형태 String 등")
    private final String tfidfVector;

    @Schema(description = "대표 썸네일 혹은 이미지 URL 목록")
    private final List<String> imageUrls;

    public SimilarProductResponseDTO(
            Long productId,
            String title,
            BigDecimal price,
            String tfidfVector,
            List<String> imageUrls
    ) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.tfidfVector = tfidfVector;
        this.imageUrls = imageUrls;
    }
    public static SimilarProductResponseDTO from(DuckuJangter product) {
        return SimilarProductResponseDTO.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .tfidfVector(product.getTfidfVector())  // product.getTfidfVector()가 String이라고 가정
                .imageUrls(
                        product.getJangterImages().stream()
                                .map(img -> img.getImage().getImageUrl())
                                .collect(Collectors.toList())
                )
                .build();
    }

    /**
     * (★) ProductWithSimilarity를 받아서 SimilarProductResponseDTO를 만드는 정적 메서드
     */
    public static SimilarProductResponseDTO from(TfidfService.ProductWithSimilarity productWithSimilarity) {
        DuckuJangter product = productWithSimilarity.getProduct();
        return SimilarProductResponseDTO.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .tfidfVector(product.getTfidfVector())
                .imageUrls(
                        product.getJangterImages().stream()
                                .map(img -> img.getImage().getImageUrl())
                                .collect(Collectors.toList())
                )
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class SimilarProductResponseDTOBuilder {
    }
}