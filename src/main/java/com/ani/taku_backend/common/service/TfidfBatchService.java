package com.ani.taku_backend.common.service;


import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.common.service.TfidfService;
import com.ani.taku_backend.common.util.TfidfCalculator;
import com.ani.taku_backend.jangter.DuckuJangter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TfidfBatchService {
    private final ExtractKeywordService extractKeywordService;
    private final TfidfService tfidfService;

    public void calculateAndCacheTfIdf(List<DuckuJangter> products) {
        log.info("Starting TF-IDF calculation for {} products", products.size());

        // 1. 각 상품의 키워드 추출 (Flask 서버와 통해)
        Map<Long, List<String>> productKeywords = extractProductKeywords(products);

        // 2. 전체 상품 수
        int totalDocs = products.size();

        // 3. 각 키워드별 빈도수 계산
        Map<String, Integer> documentFrequencies = TfidfCalculator.calculateDocumentFrequencies(productKeywords);

        // 4. 각 상품별 TF-IDF 벡터 계산 및 캐싱
        productKeywords.forEach((productId, keywords) -> {
            Map<String, Double> tfidfVector = TfidfCalculator.calculateTfIdfVector(
                    keywords,
                    documentFrequencies,
                    totalDocs
            );
            tfidfService.cacheTfidfVector(productId, tfidfVector);
        });

        log.info("Completed TF-IDF calculation and caching");
    }

    private Map<Long, List<String>> extractProductKeywords(List<DuckuJangter> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        DuckuJangter::getProductId,
                        product -> {
                            String text = product.getTitle() + " " + product.getDescription();
                            List<String> keywords = extractKeywordService.extractKeywords(text);
                            if (keywords == null || keywords.isEmpty()) {
                                log.warn("No keywords extracted for product {}", product.getProductId());
                                return List.of();
                            }
                            return keywords;
                        }
                ));
    }
}