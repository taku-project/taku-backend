package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.jangter.service.ProductImageService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 상품 이미지 URL 정보를 캡슐화하는 DTO 클래스
 * Map 대신 명확한 타입과 의미 있는 메서드를 제공합니다.
 */
public class ArticleImages {
    
    private final Map<Long, String> imageUrlByArticleId;
    
    /**
     * 상품 ID와 이미지 URL 맵으로 객체를 생성합니다.
     */
    private ArticleImages(Map<Long, String> imageUrlMap) {
        this.imageUrlByArticleId = Collections.unmodifiableMap(
            imageUrlMap != null ? new HashMap<>(imageUrlMap) : new HashMap<>()
        );
    }

    public static ArticleImages empty() {
        return new ArticleImages(new HashMap<>());
    }
    
    /**
     * 채팅방 목록에서 상품 ID를 추출하여 이미지를 조회하고 객체를 생성합니다.
     */
    public static ArticleImages fromChatRooms(
            List<ChatRoom> chatRooms, ProductImageService productImageService) {

        if (chatRooms == null || chatRooms.isEmpty() || productImageService == null) {
            return empty();
        }

        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return fromArticleIds(articleIds, productImageService);
    }

    /**
     * 상품 ID 목록으로 이미지를 조회하고 객체를 생성합니다.
     */
    public static ArticleImages fromArticleIds(
            List<Long> articleIds, ProductImageService productImageService) {
        
        if (articleIds == null || articleIds.isEmpty() || productImageService == null) {
            return empty();
        }
        
        Map<Long, String> imageUrlMap = productImageService.getProductImageMap(articleIds);
        return new ArticleImages(imageUrlMap);
    }
    

    public static ArticleImages of(Long articleId, String imageUrl) {
        Map<Long, String> map = new HashMap<>();
        if (articleId != null && imageUrl != null) {
            map.put(articleId, imageUrl);
        }
        return new ArticleImages(map);
    }

    public static ArticleImages of(Map<Long, String> imageUrlMap) {
        return new ArticleImages(imageUrlMap);
    }

    public String getImageUrl(Long articleId) {
        return imageUrlByArticleId.get(articleId);
    }

    public String getImageUrl(Long articleId, String defaultValue) {
        return imageUrlByArticleId.getOrDefault(articleId, defaultValue);
    }

    public int size() {
        return imageUrlByArticleId.size();
    }
} 