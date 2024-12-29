package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
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

    @Schema(description = "유사도 점수", example = "0.85")
    private final double similarity;

    @Schema(description = "상품 이미지 URL", example = "https://example.com/images/product.jpg")
    private final String imageUrl;

    // TODO 상품 등록 부분 구현시 연결
    public static SimilarProductResponseDTO from(CompletedDeal deal) {
        return SimilarProductResponseDTO.builder()
                .productId(deal.getId())
                .title(deal.getTitle())
                .price(deal.getPrice())
                .similarity(deal.getSimilarity())
             /*   .imageUrl(deal.getJangterImages()  // 연관된 이미지들 중 첫 번째 이미지
                        .stream()
                        .findFirst()
                        .map(JangterImage::getImageUrl)
                        .orElse(null))*/
                .build();
    }
}