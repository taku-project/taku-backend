package com.ani.taku_backend.jangter.model.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import com.ani.taku_backend.common.enums.PeriodType;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장터 랭킹 응답 DTO")
public class ProductRankInfoResponseDTO {

    @Schema(description = "기간별 랭킹 정보", example = """
        {
          "DAY": [
            {
              "rank_idx": 1,
              "product_id": 123,
              "product_name": "나루토 피규어",
              "product_image": "https://example.com/image1.jpg",
              "product_price": 50000,
              "author_name": "애니덕후"
            }
          ],
          "WEEK": [
            {
              "rank_idx": 1,
              "product_id": 456,
              "product_name": "원피스 피규어",
              "product_image": "https://example.com/image2.jpg",
              "product_price": 75000,
              "author_name": "피규어매니아"
            }
          ],
          "MONTH": [
            {
              "rank_idx": 1,
              "product_id": 789,
              "product_name": "건담 프라모델",
              "product_image": "https://example.com/image3.jpg",
              "product_price": 100000,
              "author_name": "건프라러"
            }
          ]
        }
    """)
    @JsonProperty("rank_info")
    private Map<PeriodType, List<ProductRankInfo>> rankInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "랭킹 상품 정보")
    public static class ProductRankInfo {
        @Schema(description = "랭킹 순위", example = "1")
        @JsonProperty("rank_idx")
        private int rankIdx;

        @Schema(description = "상품 ID", example = "123")
        @JsonProperty("product_id")
        private Long productId;

        @Schema(description = "상품명", example = "나루토 피규어")
        @JsonProperty("product_name")
        private String productName;

        @Schema(description = "상품 이미지 URL", example = "https://example.com/image.jpg")
        @JsonProperty("product_image")
        private String productImage;

        @Schema(description = "상품 가격", example = "50000")
        @JsonProperty("product_price")
        private BigDecimal productPrice;

        @Schema(description = "작성자 닉네임", example = "애니덕후")
        @JsonProperty("author_name")
        private String authorName;

        public static ProductRankInfo from(DuckuJangter duckuJangter) {
            return ProductRankInfo.builder()
                    .productId(duckuJangter.getId())
                    .productName(duckuJangter.getTitle())
                    .productImage(duckuJangter.getJangterImages().get(0).getImage().getImageUrl())
                    .productPrice(duckuJangter.getPrice())
                    .authorName(duckuJangter.getUser().getNickname())
                    .build();
        }
    }
}

