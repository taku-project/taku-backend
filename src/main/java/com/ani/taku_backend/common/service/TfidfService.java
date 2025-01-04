//package com.ani.taku_backend.common.service;
//
//import com.ani.taku_backend.common.enums.StatusType;
//import com.ani.taku_backend.common.util.TfidfCalculator;
//import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
//import com.ani.taku_backend.jangter.model.enums.JangterStatus;
//import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class TfidfService {
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final DuckuJangterRepository duckuJangterRepository;
//    private static final String TFIDF_CACHE_KEY = "market:tfidf:";
//
//    public Map<String, Double> getTfidfVector(Long productId) {
//        String cacheKey = TFIDF_CACHE_KEY + productId;
//        return redisTemplate.<String, Double>opsForHash().entries(cacheKey);
//    }
//
//    public void cacheTfidfVector(Long productId, Map<String, Double> tfidfVector) {
//        String cacheKey = TFIDF_CACHE_KEY + productId;
//        redisTemplate.opsForHash().putAll(cacheKey, tfidfVector);
//    }
//
//    public double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
//        double dotProduct = 0.0;
//        double norm1 = 0.0;
//        double norm2 = 0.0;
//
//        for (String key : vector1.keySet()) {
//            if (vector2.containsKey(key)) {
//                dotProduct += vector1.get(key) * vector2.get(key);
//            }
//            norm1 += vector1.get(key) * vector1.get(key);
//        }
//
//        for (double value : vector2.values()) {
//            norm2 += value * value;
//        }
//
//        if (norm1 == 0.0 || norm2 == 0.0) {
//            return 0.0;
//        }
//
//        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
//    }
//
//    @Transactional
//    public void updateProductSimilarities(String keyword, List<String> searchKeywords) {
//        // 1. 검색 키워드의 TF-IDF 벡터 계산
//        Map<String, Double> queryVector = TfidfCalculator.calculateTfIdfVector(
//                searchKeywords,
//                getDocumentFrequencies(),
//                getTotalDocuments()
//        );
//
//        // 2. 모든 상품과의 유사도 계산 및 업데이트
//        List<DuckuJangter> products = duckuJangterRepository.findByStatusAndDeletedAtIsNull(
//                JangterStatus.ON_SALE
//        );
//
//        for (DuckuJangter product : products) {
//            Map<String, Double> productVector = getTfidfVector(product.getId());
//            double similarity = calculateCosineSimilarity(queryVector, productVector);
//            product.updateSimilarity(similarity);
//        }
//
//        // 3. 변경사항 저장
//        duckuJangterRepository.saveAll(products);
//    }
//
//    private Map<String, Integer> getDocumentFrequencies() {
//        // TODO: Redis에서 문서 빈도수 조회 로직 구현
//        return redisTemplate.<String, Integer>opsForHash()
//                .entries("market:document_frequencies");
//    }
//
//    private int getTotalDocuments() {
//        // TODO: Redis에서 전체 문서 수 조회 로직 구현
//        Integer total = (Integer) redisTemplate.opsForValue().get("market:total_documents");
//        if (total != null) {
//            return total;
//        } else {
//            return 0;
//        }
//    }
//}
/*입력: "아이폰 14 pro 256gb 팝니다"

처리 과정:
        1. 복합 키워드 찾기:
        - "아이폰 14" 발견 → keywords에 추가
   - "256GB" 발견 → keywords에 추가
남은 텍스트: "pro 팝니다"

        2. 형태소 분석:
        - "pro" → keywords에 추가
   - "팝니다" → 불용어로 제거

최종 키워드: ["아이폰 14", "256GB", "pro"]*/

/*[검색어 입력] → [키워드 추출] → [유사도 계산] → [결과 반환]

예) "원피스 피규어" 검색시:
        1. 키워드 추출: ["원피스", "피규어"]
        2. 각 상품의 TF-IDF 벡터 조회
   - Redis 캐시 확인
   - 없으면 RDB 확인
   - 없으면 새로 계산
3. 유사도 계산 후 정렬*/

// 검색어 벡터와 상품의 벡터를 비교
//title과 description에서 추출된 키워드가 많이 일치할수록 유사도 높음
/*
package com.ani.taku_backend.common.service;

import com.ani.taku_backend.common.util.TfidfCalculator;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.enums.JangterStatus;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TfidfService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DuckuJangterRepository duckuJangterRepository;
    private final ExtractKeywordService extractKeywordService;

    private static final String TFIDF_CACHE_KEY = "market:tfidf:";
    private static final String DOC_FREQ_KEY = "market:document_frequencies";
    private static final String TOTAL_DOCS_KEY = "market:total_documents";
    private static final long CACHE_TTL = 3600L; // 1시간

    @PostConstruct
    public void init() {
        updateDocumentStatistics();
    }

    public Map<String, Double> getTfidfVector(Long productId) {
        String cacheKey = TFIDF_CACHE_KEY + productId;
        Map<String, Double> cached = redisTemplate.<String, Double>opsForHash().entries(cacheKey);

        if (cached.isEmpty()) {
            DuckuJangter product = duckuJangterRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            cached = calculateProductTfidfVector(product);
            cacheTfidfVector(productId, cached);
        }

        return cached;
    }

    public void cacheTfidfVector(Long productId, Map<String, Double> tfidfVector) {
        String cacheKey = TFIDF_CACHE_KEY + productId;
        redisTemplate.opsForHash().putAll(cacheKey, tfidfVector);
        redisTemplate.expire(cacheKey, CACHE_TTL, TimeUnit.SECONDS);
    }

    @Transactional
    public void updateProductSimilarities(String keyword, List<String> searchKeywords) {
        Map<String, Integer> documentFrequencies = getDocumentFrequencies();
        int totalDocuments = getTotalDocuments();

        if (documentFrequencies.isEmpty() || totalDocuments == 0) {
            updateDocumentStatistics();
            documentFrequencies = getDocumentFrequencies();
            totalDocuments = getTotalDocuments();
        }

        Map<String, Double> queryVector = TfidfCalculator.calculateTfIdfVector(
                searchKeywords,
                documentFrequencies,
                totalDocuments
        );

        List<DuckuJangter> products = duckuJangterRepository.findByStatusAndDeletedAtIsNull(
                JangterStatus.ON_SALE
        );

        for (DuckuJangter product : products) {
            Map<String, Double> productVector = getTfidfVector(product.getId());
            double similarity = calculateCosineSimilarity(queryVector, productVector);
            product.updateSimilarity(similarity);
        }

        duckuJangterRepository.saveAll(products);
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 갱신
    public void updateDocumentStatistics() {
        try {
            List<DuckuJangter> allProducts = duckuJangterRepository.findAll();
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
            redisTemplate.expire(DOC_FREQ_KEY, CACHE_TTL, TimeUnit.SECONDS);
            redisTemplate.expire(TOTAL_DOCS_KEY, CACHE_TTL, TimeUnit.SECONDS);

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
}*/
