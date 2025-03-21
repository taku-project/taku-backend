package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 상품 이미지 정보를 표현하는 Value Object
 */
public class ArticleImage {

    private final List<ArticleImageItem> imageItems;
    
    private ArticleImage(List<ArticleImageItem> imageItems) {
        this.imageItems = Collections.unmodifiableList(
            imageItems != null ? new ArrayList<>(imageItems) : new ArrayList<>()
        );
    }

    public static class ArticleImageItem {
        private final Long articleId;
        private final String imageUrl;
        
        private ArticleImageItem(Long articleId, String imageUrl) {
            this.articleId = Objects.requireNonNull(articleId, "상품 ID는 null일 수 없습니다");
            this.imageUrl = imageUrl; // imageUrl은 null 허용
        }
        
        public Long getArticleId() {
            return articleId;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
    }
    

    public static ArticleImage empty() {
        return new ArticleImage(List.of());
    }
    

    public static ArticleImage of(Long articleId, String imageUrl) {
        if (articleId == null) {
            return empty();
        }
        
        return new ArticleImage(List.of(new ArticleImageItem(articleId, imageUrl)));
    }
    

    public static ArticleImage fromProductImageDTOs(List<ProductImageDTO> productImages) {
        if (productImages == null || productImages.isEmpty()) {
            return empty();
        }
        
        List<ArticleImageItem> items = productImages.stream()
            .filter(dto -> dto.productId() != null)
            .map(dto -> new ArticleImageItem(dto.productId(), dto.imageUrl()))
            .collect(Collectors.toList());
        
        return new ArticleImage(items);
    }

    public String getImageUrl(Long articleId) {
        if (articleId == null) {
            return null;
        }
        
        return imageItems.stream()
            .filter(item -> articleId.equals(item.getArticleId()))
            .map(ArticleImageItem::getImageUrl)
            .findFirst()
            .orElse(null);
    }

} 