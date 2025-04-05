package com.ani.taku_backend.jangter.model.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 채팅방에서 사용되는 상품 기본 정보 DTO
 * 상품 ID, 제목, 가격, 이미지 URL만 포함하여 경량화
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleInfoDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String imageUrl;
} 