package com.ani.taku_backend.common.util;


import com.ani.taku_backend.common.service.TfidfBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*@Component
@RequiredArgsConstructor
@Slf4j
public class TfidfBatchScheduler {
    /*private final TfidfBatchService tfidfBatchService;
   // 장터 레파지토리
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    public void calculateTfidf() {
        log.info("Starting TF-IDF calculation batch job");
        try {
            tfidfBatchService.calculateAndCacheTfIdf(
                   // 더쿠 레파지토리. findAll()
            );
            log.info("Successfully completed TF-IDF calculation batch job");
        } catch (Exception e) {
            log.error("Failed to calculate TF-IDF", e);
        }
    }


}

 */