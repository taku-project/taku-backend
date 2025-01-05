package com.ani.taku_backend.common.util;

import lombok.experimental.UtilityClass;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class TfidfCalculator {

    public Map<String, Double> calculateTfIdfVector(
            List<String> keywords,
            Map<String, Integer> documentFrequencies,
            int totalDocs
    ) {
        if (keywords == null || keywords.isEmpty() || totalDocs <= 0) {
            return Map.of();
        }

        // 키워드 빈도수 계산 (TF)
        Map<String, Long> termFrequencies = keywords.stream()
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.counting()
                ));

        // TF-IDF 벡터 계산
        return termFrequencies.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)  // 0인 빈도수 제외
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateTfIdf(
                                entry.getValue(),
                                keywords.size(),
                                documentFrequencies.getOrDefault(entry.getKey(), 0),
                                totalDocs
                        )
                ));
    }

    public Map<String, Integer> calculateDocumentFrequencies(Map<Long, List<String>> productKeywords) {
        if (productKeywords == null || productKeywords.isEmpty()) {
            return Map.of();
        }

        return productKeywords.values().stream()
                .filter(keywords -> keywords != null && !keywords.isEmpty())
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private double calculateTfIdf(long termCount, int totalTerms, int documentFrequency, int totalDocs) {
        double tf = (double) termCount / totalTerms;

        double idf = Math.log((double) (totalDocs + 1) / (documentFrequency + 1)) + 1.0;

        return tf * idf;
    }
}