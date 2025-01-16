package com.ani.taku_backend.jangter.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.JangterImages;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendResponseDTO {

    private List<RecommendProduct> recommendProducts;

    public static ProductRecommendResponseDTO of(List<DuckuJangter> recommendProducts) {
        return ProductRecommendResponseDTO.builder()
            .recommendProducts(recommendProducts.stream().map(RecommendProduct::of).toList())
            .build();
    }

    public static ProductRecommendResponseDTO empty() {
        return ProductRecommendResponseDTO.builder()
            .recommendProducts(Collections.emptyList())
            .build();
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendProduct {

        @JsonProperty("product_id")
        private Long id;

        @JsonProperty("title")
        private String title;

        @JsonProperty("view_count")
        private long viewCount;

        @JsonProperty("price")
        private BigDecimal price;

        @JsonProperty("item_category_id")
        private Long itemCategoryId;

        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;

        @JsonProperty("created_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;

        public static RecommendProduct of(DuckuJangter duckuJangter) {

            String imageUrl = duckuJangter.getJangterImages().stream().findFirst().map(img -> img.getImage().getImageUrl()).orElse(null);

            return RecommendProduct.builder()
                .id(duckuJangter.getId())
                .title(duckuJangter.getTitle())
                .viewCount(duckuJangter.getViewCount())
                .price(duckuJangter.getPrice())
                .itemCategoryId(duckuJangter.getItemCategories().getId())
                .thumbnailUrl(imageUrl)
                .createdAt(duckuJangter.getCreatedAt())
                .build();
        }

    }
}

