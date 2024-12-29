package com.ani.taku_backend.marketprice.service;

import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.*;
import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletedDealService {
    private final CompletedDealRepository completedDealRepository;
    private final ExtractKeywordService extractKeywordService;

    @Transactional(readOnly = true)
    @Cacheable(value = "marketPrice", key = "#keyword + #fromDate + #toDate + #option")
    public MarketPriceSearchResponseDTO searchMarketPrice(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            GraphDisplayOption option,
            Pageable pageable
    ) {
        try {
            // 1. 키워드 추출
            List<String> extractedKeywords = extractKeywordService.extractKeywords(keyword);
            String processedKeyword = String.join(" ", extractedKeywords);

            // 2. 시세 그래프 데이터 조회
            PriceGraphResponseDTO priceGraph = completedDealRepository
                    .getPriceGraph(processedKeyword, fromDate, toDate, option);

            // 3. 최근 일주일 통계 조회
            WeeklyStatsResponseDTO weeklyStats = completedDealRepository
                    .getWeeklyStats(processedKeyword);

            // 4. 유사 상품 조회
            List<SimilarProductResponseDTO> similarProducts = completedDealRepository
                    .findSimilarProducts(processedKeyword, pageable);

            return MarketPriceSearchResponseDTO.builder()
                    .keyword(keyword)
                    .priceGraph(priceGraph)
                    .weeklyStats(weeklyStats)
                    .similarProducts(similarProducts)
                    .build();

        } catch (Exception e) {
            log.error("시세 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("시세 조회 중 오류가 발생했습니다.", e);
        }
    }
}