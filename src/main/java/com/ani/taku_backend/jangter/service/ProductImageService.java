package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 이미지 URL 조회 관련 기능을 제공하는 서비스
 */
@Service
@Transactional(readOnly = true)
public class ProductImageService {

    private final DuckuJangterRepository duckuJangterRepository;

    public ProductImageService(DuckuJangterRepository duckuJangterRepository) {
        this.duckuJangterRepository = duckuJangterRepository;
    }

    /**
     * 상품 ID 목록에 대한 이미지 VO를 생성합니다.
     * 
     * @param productIds 상품 ID 목록
     * @return 상품 이미지 Value Object
     */
    public ArticleImage getArticleImages(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ArticleImage.empty();
        }
        
        Map<Long, String> imageMap = getProductImageMap(productIds);
        return ArticleImage.of(imageMap);
    }
    
    /**
     * 단일 상품에 대한 이미지 VO를 생성합니다.
     * 
     * @param productId 상품 ID
     * @return 상품 이미지 Value Object
     */
    public ArticleImage getArticleImage(Long productId) {
        if (productId == null) {
            return ArticleImage.empty();
        }
        
        String imageUrl = getProductImageUrl(productId);
        return ArticleImage.of(productId, imageUrl);
    }

    /**
     * 상품 ID 목록에 해당하는 이미지 URL 맵을 생성합니다.
     * 하위 호환성을 위해 유지합니다.
     *
     * @param productIds 상품 ID 목록
     * @return 상품 ID를 키로, 이미지 URL을 값으로 하는 맵
     */
    public Map<Long, String> getProductImageMap(List<Long> productIds) {
        Map<Long, String> articleImageMap = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return articleImageMap;
        }

        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(productIds);
        for (ProductImageDTO productImage : productImages) {
            articleImageMap.put(productImage.productId(), productImage.imageUrl());
        }
        return articleImageMap;
    }

    /**
     * 단일 상품의 이미지 URL을 조회합니다.
     * 하위 호환성을 위해 유지합니다.
     *
     * @param productId 상품 ID
     * @return 이미지 URL 또는 null
     */
    public String getProductImageUrl(Long productId) {
        if (productId == null) {
            return null;
        }

        return duckuJangterRepository.findWithDetailsById(productId)
                .map(product -> product.getJangterImages().stream()
                        .findFirst()
                        .map(img -> img.getImage().getImageUrl())
                        .orElse(null))
                .orElse(null);
    }
}