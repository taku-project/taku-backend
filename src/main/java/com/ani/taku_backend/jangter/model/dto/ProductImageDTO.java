package com.ani.taku_backend.jangter.model.dto;

/**
 * 상품 ID와 이미지 URL 정보를 담는 DTO
 */
public record ProductImageDTO(
    Long productId,
    String imageUrl
) {
} 