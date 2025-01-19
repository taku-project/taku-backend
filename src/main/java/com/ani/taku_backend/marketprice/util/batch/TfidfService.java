package com.ani.taku_backend.marketprice.util.batch;

import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TfidfService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DuckuJangterRepository duckuJangterRepository;
    private final ExtractKeywordService extractKeywordService;
    private final ObjectMapper objectMapper;

    private static final String TFIDF_CACHE_KEY = "market:tfidf:";
    private static final String DOC_FREQ_KEY = "market:document_frequencies";
    private static final String TOTAL_DOCS_KEY = "market:total_documents";

    private static final long CACHE_TTL_SECONDS = 3600L; // 1시간,3600초

    // 1시간마다 갱신(밀리초로 3600000)
    private static final long DOCUMENT_STATS_UPDATE_RATE = 3600000L;

    @PostConstruct
    public void init() {
        // updateDocumentStatistics();
    }

    public Map<String, Double> getTfidfVector(Long productId) {
        String cacheKey = TFIDF_CACHE_KEY + productId;
        Map<String, Double> cached = redisTemplate.<String, Double>opsForHash().entries(cacheKey);

        if (cached.isEmpty()) {
            DuckuJangter product = duckuJangterRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (product.getTfidfVector() != null) {
                try {
                    cached = objectMapper.readValue(
                            product.getTfidfVector(),
                            new TypeReference<Map<String, Double>>() {}
                    );
                    cacheTfidfVector(productId, cached);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse TF-IDF vector from database", e);
                    cached = calculateProductTfidfVector(product);
                    updateProductTfidfVector(product, cached);
                }
            } else {
                cached = calculateProductTfidfVector(product);
                updateProductTfidfVector(product, cached);
            }
        }

        return cached;
    }

    @Transactional
    public void updateProductTfidfVector(DuckuJangter product, Map<String, Double> vector) {
        try {
            String vectorJson = objectMapper.writeValueAsString(vector);
            product.updateTfidfVector(vectorJson);
            duckuJangterRepository.save(product);
            cacheTfidfVector(product.getId(), vector);
        } catch (JsonProcessingException e) {
            log.error("Failed to update TF-IDF vector", e);
        }
    }

    public void cacheTfidfVector(Long productId, Map<String, Double> tfidfVector) {
        String cacheKey = TFIDF_CACHE_KEY + productId;
        redisTemplate.opsForHash().putAll(cacheKey, tfidfVector);
        redisTemplate.expire(cacheKey, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public List<ProductWithSimilarity> calculateProductSimilarities(String keyword, List<String> searchKeywords) {
        Map<String, Integer> documentFrequencies = getDocumentFrequencies();
        int totalDocuments = getTotalDocuments();

        Map<String, Double> queryVector = TfidfCalculator.calculateTfIdfVector(
                searchKeywords,
                documentFrequencies,
                totalDocuments
        );

        List<DuckuJangter> products = duckuJangterRepository.findByDeletedAtIsNull();

        return products.stream()
                .map(product -> {
                    Map<String, Double> productVector = getTfidfVector(product.getId());
                    double similarity = calculateCosineSimilarity(queryVector, productVector);
                    return new ProductWithSimilarity(product, similarity);
                })
                .sorted(Comparator.comparing(ProductWithSimilarity::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = DOCUMENT_STATS_UPDATE_RATE) // 1시간마다 갱신
    public void updateDocumentStatistics() {
        try {
            List<DuckuJangter> allProducts = duckuJangterRepository.findByDeletedAtIsNull();
            Map<String, Integer> frequencies = new HashMap<>();

            for (DuckuJangter product : allProducts) {
                List<String> titleTerms = extractTerms(product.getTitle());
                List<String> descTerms = extractTerms(product.getDescription());

                List<String> allTerms = new ArrayList<>();
                allTerms.addAll(titleTerms);
                allTerms.addAll(descTerms);

                for (String term : allTerms) {
                    frequencies.merge(term, 1, Integer::sum);
                }
            }

            redisTemplate.opsForHash().putAll(DOC_FREQ_KEY, frequencies);
            redisTemplate.opsForValue().set(TOTAL_DOCS_KEY, allProducts.size());
            redisTemplate.expire(DOC_FREQ_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            redisTemplate.expire(TOTAL_DOCS_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

            log.info("Document statistics updated. Total documents: {}", allProducts.size());
        } catch (Exception e) {
            log.error("Failed to update document statistics", e);
        }
    }

    private Map<String, Integer> getDocumentFrequencies() {
        Map<String, Integer> frequencies = redisTemplate.<String, Integer>opsForHash()
                .entries(DOC_FREQ_KEY);

        if (frequencies.isEmpty()) {
            updateDocumentStatistics();
            frequencies = redisTemplate.<String, Integer>opsForHash()
                    .entries(DOC_FREQ_KEY);
        }

        return frequencies;
    }

    private int getTotalDocuments() {
        Integer total = (Integer) redisTemplate.opsForValue().get(TOTAL_DOCS_KEY);
        if (total == null) {
            updateDocumentStatistics();
            total = (Integer) redisTemplate.opsForValue().get(TOTAL_DOCS_KEY);
        }
        return total != null ? total : 0;
    }

    private Map<String, Double> calculateProductTfidfVector(DuckuJangter product) {
        List<String> titleTerms = extractTerms(product.getTitle());
        List<String> descTerms = extractTerms(product.getDescription());

        List<String> allTerms = new ArrayList<>();
        allTerms.addAll(titleTerms);
        allTerms.addAll(descTerms);

        return TfidfCalculator.calculateTfIdfVector(
                allTerms,
                getDocumentFrequencies(),
                getTotalDocuments()
        );
    }

    private List<String> extractTerms(String text) {
        List<String> keywords = extractKeywordService.extractKeywords(text);
        if (keywords == null || keywords.isEmpty()) {
            log.warn("Failed to extract keywords from text, falling back to simple split: {}", text);
            return List.of(text.toLowerCase().split("\\s+"));
        }
        return keywords;
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

    @Getter
    public static class ProductWithSimilarity {
        private final DuckuJangter product;
        private final double similarity;

        public ProductWithSimilarity(DuckuJangter product, double similarity) {
            this.product = product;
            this.similarity = similarity;
        }
    }
}