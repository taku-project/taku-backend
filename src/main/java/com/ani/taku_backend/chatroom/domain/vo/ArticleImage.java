package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            this.articleId = Objects.requireNonNull(articleId, MessageConstants.ARTICLE_ID_NOT_NULL);
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
    

    public static ArticleImage of(Map<Long, String> imageUrlMap) {
        if (imageUrlMap == null || imageUrlMap.isEmpty()) {
            return empty();
        }
        
        List<ArticleImageItem> items = imageUrlMap.entrySet().stream()
            .filter(entry -> entry.getKey() != null)
            .map(entry -> new ArticleImageItem(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
            
        return new ArticleImage(items);
    }

    public static ArticleImage fromProductImageDTOs(List<ProductImageDTO> productImages) {
        if (productImages == null || productImages.isEmpty()) {
            return empty();
        }
        
        List<ArticleImageItem> items = productImages.stream()
            .filter(dto -> dto != null && dto.productId() != null)
            .map(dto -> new ArticleImageItem(dto.productId(), dto.imageUrl()))
            .collect(Collectors.toList());
        
        return new ArticleImage(items);
    }

    public String getImageUrl(Long articleId) {
        if (articleId == null || imageItems.isEmpty()) {
            return null;
        }
        
        return imageItems.stream()
            .filter(Objects::nonNull)
            .filter(item -> articleId.equals(item.getArticleId()))
            .map(ArticleImageItem::getImageUrl)
            .findFirst()
            .orElse(null);
    }

    public List<ArticleImageItem> getImageItems() {
        return imageItems;
    }

} 