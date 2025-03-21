package com.ani.taku_backend.chatroom.domain.vo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArticleImage {
    
    private final Map<Long, String> imageUrlByArticleId;

    private ArticleImage(Map<Long, String> imageUrlMap) {
        this.imageUrlByArticleId = Collections.unmodifiableMap(
            imageUrlMap != null ? new HashMap<>(imageUrlMap) : new HashMap<>()
        );
    }

    public static ArticleImage empty() {
        return new ArticleImage(new HashMap<>());
    }


    public static ArticleImage of(Long articleId, String imageUrl) {
        Map<Long, String> map = new HashMap<>();
        if (articleId != null && imageUrl != null) {
            map.put(articleId, imageUrl);
        }
        return new ArticleImage(map);
    }

    public static ArticleImage of(Map<Long, String> imageUrlMap) {
        return new ArticleImage(imageUrlMap);
    }


    public String getImageUrl(Long articleId) {
        if (articleId == null) {
            return null;
        }
        return imageUrlByArticleId.get(articleId);
    }
} 