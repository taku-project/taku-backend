package com.ani.taku_backend.jangter.util.batch;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JangterRankScheduler {

    // 매일 1시에 실행
    private static final String JANGTER_DAILY_RANK_CRON_EXPRESSION = "0 0 1 * * *";

    private final DuckuJangterRepository duckuJangterRepository;

    @Scheduled(cron = JANGTER_DAILY_RANK_CRON_EXPRESSION)
    public void createJangterDailyRank() {
        log.info("장터 랭킹 생성 시작");

        // 장터 랭킹 생성

        List<CategoryGroupCountDTO> categoryGroupCount = duckuJangterRepository.findCategoryGroupCount();
        log.info("카테고리 그룹 조회 완료 : {}", categoryGroupCount);

        // TODO : 오늘자 미판매 장터 물품 전체 조회
        // -> 카테고리별 그룹화 하여 병렬처리

        // 각 카테고리별 조회수 , 북마크 조회

        // 스코어링

        // 데이터 셋 저장

        // 랭킹 저장

    }





    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("장터 랭킹 생성 초기화 완료");
        createJangterDailyRank();



    }
}
