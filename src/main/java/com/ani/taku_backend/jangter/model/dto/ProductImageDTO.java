package com.ani.taku_backend.jangter.model.dto;

/**
 * 상품 ID와 이미지 URL 정보만 담는 간단한 DTO
 */
public record ProductImageDTO(
    Long productId,
    String imageUrl
) {
} 