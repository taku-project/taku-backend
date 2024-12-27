package com.ani.taku_backend.common.service;

import com.ani.taku_backend.common.util.TfidfCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TfidfService {
    private final RedisTemplate<String, Object> redisTemplate;
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

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}