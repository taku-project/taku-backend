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
        // 키워드 빈도수 계산 (TF)
        Map<String, Long> termFrequencies = keywords.stream()
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.counting()
                ));

        // TF-IDF 벡터 계산
        return termFrequencies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            double tf = (double) entry.getValue() / keywords.size();
                            double idf = Math.log((double) totalDocs /
                                    (1 + documentFrequencies.getOrDefault(entry.getKey(), 0)));
                            return tf * idf;
                        }
                ));
    }

    public Map<String, Integer> calculateDocumentFrequencies(Map<Long, List<String>> productKeywords) {
        return productKeywords.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
}
