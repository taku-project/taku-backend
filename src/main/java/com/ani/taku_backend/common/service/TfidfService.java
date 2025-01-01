package com.ani.taku_backend.common.service;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.util.TfidfCalculator;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.enums.JangterStatus;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TfidfService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DuckuJangterRepository duckuJangterRepository;
    private static final String TFIDF_CACHE_KEY = "market:tfidf:";

    public Map<String, Double> getTfidfVector(Long productId) {
        String cacheKey = TFIDF_CACHE_KEY + productId;
        return redisTemplate.<String, Double>opsForHash().entries(cacheKey);
    }

    public void cacheTfidfVector(Long productId, Map<String, Double> tfidfVector) {
        String cacheKey = TFIDF_CACHE_KEY + productId;
        redisTemplate.opsForHash().putAll(cacheKey, tfidfVector);
    }

    public double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : vector1.keySet()) {
            if (vector2.containsKey(key)) {
                dotProduct += vector1.get(key) * vector2.get(key);
            }
            norm1 += vector1.get(key) * vector1.get(key);
        }

        for (double value : vector2.values()) {
            norm2 += value * value;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Transactional
    public void updateProductSimilarities(String keyword, List<String> searchKeywords) {
        // 1. 검색 키워드의 TF-IDF 벡터 계산
        Map<String, Double> queryVector = TfidfCalculator.calculateTfIdfVector(
                searchKeywords,
                getDocumentFrequencies(),
                getTotalDocuments()
        );

        // 2. 모든 상품과의 유사도 계산 및 업데이트
        List<DuckuJangter> products = duckuJangterRepository.findByStatusAndDeletedAtIsNull(
                JangterStatus.ON_SALE
        );

        for (DuckuJangter product : products) {
            Map<String, Double> productVector = getTfidfVector(product.getId());
            double similarity = calculateCosineSimilarity(queryVector, productVector);
            product.updateSimilarity(similarity);
        }

        // 3. 변경사항 저장
        duckuJangterRepository.saveAll(products);
    }

    private Map<String, Integer> getDocumentFrequencies() {
        // TODO: Redis에서 문서 빈도수 조회 로직 구현
        return redisTemplate.<String, Integer>opsForHash()
                .entries("market:document_frequencies");
    }

    private int getTotalDocuments() {
        // TODO: Redis에서 전체 문서 수 조회 로직 구현
        Integer total = (Integer) redisTemplate.opsForValue().get("market:total_documents");
        if (total != null) {
            return total;
        } else {
            return 0;
        }
    }
}