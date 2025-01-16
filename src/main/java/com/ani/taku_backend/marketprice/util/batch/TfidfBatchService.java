package com.ani.taku_backend.marketprice.util.batch;


import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import jakarta.transaction.Transactional;
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
    private final DuckuJangterRepository duckuJangterRepository;

    @Transactional
    public void calculateAndCacheTfIdf(List<DuckuJangter> products) {
        log.info("Starting TF-IDF calculation for {} products", products.size());

        // 1. 각 상품의 키워드 추출
        Map<Long, List<String>> productKeywords = extractProductKeywords(products);

        // 2. 전체 상품 수
        int totalDocs = products.size();

        // 3. 각 키워드별 빈도수 계산
        Map<String, Integer> documentFrequencies = TfidfCalculator.calculateDocumentFrequencies(productKeywords);

        // 4. 각 상품별 TF-IDF 벡터 계산 및 저장 (캐시 + RDB)
        List<DuckuJangter> updatedProducts = products.stream()
                .map(product -> {
                    List<String> keywords = productKeywords.get(product.getId());
                    Map<String, Double> tfidfVector = TfidfCalculator.calculateTfIdfVector(
                            keywords,
                            documentFrequencies,
                            totalDocs
                    );

                    // Redis 캐시와 RDB에 모두 저장
                    tfidfService.updateProductTfidfVector(product, tfidfVector);
                    return product;
                })
                .collect(Collectors.toList());

        // 5. 벌크 업데이트
        duckuJangterRepository.saveAll(updatedProducts);

        log.info("Completed TF-IDF calculation and storage for {} products", products.size());
    }

    private Map<Long, List<String>> extractProductKeywords(List<DuckuJangter> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        DuckuJangter::getId,
                        product -> {
                            String text = product.getTitle() + " " + product.getDescription();
                            List<String> keywords = extractKeywordService.extractKeywords(text);
                            if (keywords == null || keywords.isEmpty()) {
                                log.warn("No keywords extracted for product {}", product.getId());
                                return List.of();
                            }
                            return keywords;
                        }
                ));
    }
}