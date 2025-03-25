package com.ani.taku_backend.jangter.util.batch;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JangterRankScheduler {

    // 매일 1시에 실행
    private static final String JANGTER_DAILY_RANK_CRON_EXPRESSION = "0 0 0 * * *";

    private static final String JANGTER_WEEKLY_RANK_CRON_EXPRESSION = "0 0 0 ? * MON";

    // 매월 1일 0시에 실행
    private static final String JANGTER_MONTHLY_RANK_CRON_EXPRESSION = "0 0 0 1 * ?";

    private final JangterRankBatchService jangterRankBatchService;
    @Scheduled(cron = JANGTER_DAILY_RANK_CRON_EXPRESSION)
    public void createJangterDailyRank() {
        log.info("장터 일별 랭킹 생성 시작");

        jangterRankBatchService.createJangterDailyRank();
        log.info("장터 일별 랭킹 생성 완료");
    }

    @Scheduled(cron = JANGTER_WEEKLY_RANK_CRON_EXPRESSION)
    public void createJangterWeeklyRank() {
        log.info("장터 주간 랭킹 생성 시작");
        jangterRankBatchService.createJangterWeeklyRank();
        log.info("장터 주간 랭킹 생성 완료");
    }

    @Scheduled(cron = JANGTER_MONTHLY_RANK_CRON_EXPRESSION)
    public void createJangterMonthlyRank() {
        log.info("장터 월간 랭킹 생성 시작");
        jangterRankBatchService.createJangterMonthlyRank();
        log.info("장터 월간 랭킹 생성 완료");
    }


    /**
     * local test 용도
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // createJangterDailyRank();
        // createJangterWeeklyRank();
        // createJangaterMonthlyRank();
    }
}
