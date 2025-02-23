package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkListResponseDTO {
    private Long productId;
    private String title;
    private BigDecimal price;
    private String imageUrl;
    private String sellerNickname;
    private Long viewCount;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime bookmarkedAt;


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
                .sellerNickname(jangter.getUser().getNickname())
                .viewCount(jangter.getViewCount())
                .categoryId(jangter.getItemCategories().getId())
                .categoryName(jangter.getItemCategories().getName())
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }
}