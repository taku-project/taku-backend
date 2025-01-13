package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

/**
 * 유사 상품 정보 DTO
 */
@Getter
@Schema(description = "유사 상품 정보 DTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class SimilarProductResponseDTO {

    @Schema(description = "상품 ID", example = "123")
    private final Long productId;

    @Schema(description = "상품 제목", example = "원피스 루피 피규어")
    private final String title;

    @Schema(description = "등록 가격", example = "21000")
    private final BigDecimal price;

    @Schema(description = "TF-IDF 벡터 값(예시)", example = "JSON 형태 String 등")
    private final String tfidfVector;

    @Schema(description = "대표 썸네일 (단일 이미지 URL)")
    private final List<String> imageUrls;

    @JsonCreator
    public SimilarProductResponseDTO(
            @JsonProperty("productId") Long productId,
            @JsonProperty("title") String title,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("tfidfVector") String tfidfVector,
            @JsonProperty("imageUrl") String imageUrl
    ) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.tfidfVector = tfidfVector;

        if (imageUrl != null) {
            this.imageUrls = Collections.singletonList(imageUrl);
        } else {
            this.imageUrls = Collections.emptyList();
        }
    }

    public static SimilarProductResponseDTO from(DuckuJangter product) {
        // 썸네일을 대표 이미지 1장만 뽑는다. 수정 가능성 존재
        String singleImageUrl = product.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);

        return new SimilarProductResponseDTO(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getTfidfVector(),   // String 형태로 가정
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