package com.ani.taku_backend.bookmark.domain.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class DuckuJangterBookmarkResponseDTO {
    private Long productId;
    private String title;
    private BigDecimal price;
    private String status;
    private String categoryName;
    private LocalDateTime bookmarkedAt;

    @Builder
    public DuckuJangterBookmarkResponseDTO(Long productId, String title, BigDecimal price,
                                           String status, String categoryName, LocalDateTime bookmarkedAt) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.status = status;
        this.categoryName = categoryName;
        this.bookmarkedAt = bookmarkedAt;
    }

    public static DuckuJangterBookmarkResponseDTO from(DuckuJangterBookmark bookmark) {
        return DuckuJangterBookmarkResponseDTO.builder()
                .productId(bookmark.getJangter().getId())
                .title(bookmark.getJangter().getTitle())
                .price(bookmark.getJangter().getPrice())
                .status(bookmark.getJangter().getStatus().name())
                .categoryName(bookmark.getJangter().getItemCategories().getName())
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }
}