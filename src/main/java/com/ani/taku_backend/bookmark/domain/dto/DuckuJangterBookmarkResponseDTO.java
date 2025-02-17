package com.ani.taku_backend.bookmark.domain.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuckuJangterBookmarkResponseDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static DuckuJangterBookmarkResponseDTO from(DuckuJangterBookmark bookmark) {
        String imageUrl = bookmark.getJangter().getJangterImages().isEmpty() ? 
            null : 
            bookmark.getJangter().getJangterImages().get(0).getImage().getImageUrl();
            
        return DuckuJangterBookmarkResponseDTO.builder()
                .id(bookmark.getJangter().getId())
                .title(bookmark.getJangter().getTitle())
                .price(bookmark.getJangter().getPrice())
                .category(bookmark.getJangter().getItemCategories().getName())
                .imageUrl(imageUrl)
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}