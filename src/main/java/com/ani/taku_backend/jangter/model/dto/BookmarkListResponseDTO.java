package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkListResponseDTO {
    private Long productId;
    private String title;
    private BigDecimal price;
    private Long viewCount;
    private Long categoryId;
    private String imageUrl;

    private static String extractImageUrl(DuckuJangter jangter) {
        return jangter.getJangterImages().stream()
                .findFirst()
                .map(img -> img.getImage().getImageUrl())
                .orElse(null);
    }


    public static BookmarkListResponseDTO from(DuckuJangterBookmark bookmark) {
        DuckuJangter jangter = bookmark.getJangter();
        return BookmarkListResponseDTO.builder()
                .productId(jangter.getId())
                .title(jangter.getTitle())
                .price(jangter.getPrice())
                .imageUrl(extractImageUrl(jangter))
                .viewCount(jangter.getViewCount())
                .categoryId(jangter.getItemCategories().getId())
                .build();
    }
}