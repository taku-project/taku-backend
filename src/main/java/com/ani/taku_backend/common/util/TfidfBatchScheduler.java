package com.ani.taku_backend.common.util;


import com.ani.taku_backend.common.service.TfidfBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TfidfBatchScheduler {
    private final TfidfBatchService tfidfBatchService;
    private final DuckuJangterRepository duckuJangterRepository;

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    public void calculateTfidf() {
        log.info("Starting TF-IDF calculation batch job");
        try {
            // 삭제되지 않은 상품만 조회
            List<DuckuJangter> activeProducts = duckuJangterRepository.findByDeletedAtIsNull();

            if (activeProducts.isEmpty()) {
                log.warn("No active products found for TF-IDF calculation");
                return;
            }

            tfidfBatchService.calculateAndCacheTfIdf(activeProducts);
            log.info("Successfully completed TF-IDF calculation batch job for {} products",
                    activeProducts.size());
        } catch (Exception e) {
            log.error("Failed to calculate TF-IDF: {}", e.getMessage(), e);
        }
    }
}