package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.jangter.model.dto.ArticleInfoDTO;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 채팅방에서 사용되는 상품 정보를 담는 Value Object입니다.
 */
@Value
public class ArticleInfo {
    
    List<ArticleData> articles;
    
    private ArticleInfo(List<ArticleData> articles) {
        this.articles = articles != null ? 
            Collections.unmodifiableList(articles) : 
            Collections.emptyList();
    }
    
    @Value
    public static class ArticleData {
        Long articleId;
        String title;
        BigDecimal price;
        String imageUrl;
        
        public static ArticleData from(ArticleInfoDTO dto) {
            if (dto == null) {
                return null;
            }
            return new ArticleData(
                dto.getId(),
                dto.getTitle(),
                dto.getPrice(),
                dto.getImageUrl()
            );
        }
    }
    
    public static ArticleInfo from(List<ArticleInfoDTO> articleInfoList) {
        if (articleInfoList == null || articleInfoList.isEmpty()) {
            return empty();
        }
        
        List<ArticleData> articleDataList = articleInfoList.stream()
            .filter(Objects::nonNull)
            .map(ArticleData::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            
        return new ArticleInfo(articleDataList);
    }
    
    public static ArticleInfo empty() {
        return new ArticleInfo(Collections.emptyList());
    }
    
    public String getTitle(Long articleId) {
        return findArticle(articleId)
            .map(ArticleData::getTitle)
            .orElse(null);
    }
    
    public BigDecimal getPrice(Long articleId) {
        return findArticle(articleId)
            .map(ArticleData::getPrice)
            .orElse(null);
    }
    
    public String getImageUrl(Long articleId) {
        return findArticle(articleId)
            .map(ArticleData::getImageUrl)
            .orElse(null);
    }
    
    private java.util.Optional<ArticleData> findArticle(Long articleId) {
        if (articleId == null) {
            return java.util.Optional.empty();
        }
        
        return articles.stream()
            .filter(data -> Objects.equals(data.getArticleId(), articleId))
            .findFirst();
    }

} 