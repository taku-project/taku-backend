package com.ani.taku_backend.marketprice.service;

import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
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
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletedDealService {
    private final CompletedDealRepository completedDealRepository;
    private final ExtractKeywordService extractKeywordService;

    @Transactional(readOnly = true)
    public MarketPriceSearchResponseDTO searchMarketPrice(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            GraphDisplayOption option,
            Pageable pageable
    ) {
        try {
            List<String> extractedKeywords = extractKeywordService.extractKeywords(keyword);
            String processedKeyword = String.join(" ", extractedKeywords);

            return MarketPriceSearchResponseDTO.builder()
                    .keyword(keyword)
                    .priceGraph(getPriceGraphWithCache(processedKeyword, fromDate, toDate, option))
                    .weeklyStats(getWeeklyStatsWithCache(processedKeyword))
                    .similarProducts(getSimilarProductsWithCache(processedKeyword, pageable))
                    .build();

        } catch (Exception e) {
            log.error("시세 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("시세 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DuckuJangter> findRecentDealsByCategory(ItemCategories category, LocalDateTime startDate) {
        return completedDealRepository.findByItemCategoriesAndCreatedAtAfterOrderByCreatedAtDesc(
                category,
                startDate
        );
    }

    // 시세 그래프 - 긴 TTL로 캐싱 (예: 1시간)
    @Cacheable(
            value = "priceGraph",
            key = "#keyword + #fromDate + #toDate + #option",
            unless = "#result == null"
    )
    public PriceGraphResponseDTO getPriceGraphWithCache(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            GraphDisplayOption option
    ) {
        return completedDealRepository.getPriceGraph(keyword, fromDate, toDate, option);
    }

    // 주간 통계 - 짧은 TTL로 캐싱 (예: 5분)
    @Cacheable(
            value = "weeklyStats",
            key = "#keyword",
            unless = "#result == null"
    )
    public WeeklyStatsResponseDTO getWeeklyStatsWithCache(String keyword) {
        return completedDealRepository.getWeeklyStats(keyword);
    }

    // 유사 상품 - 중간 TTL로 캐싱 (예: 30분)
    @Cacheable(
            value = "similarProducts",
            key = "#keyword + #pageable.pageNumber + #pageable.pageSize",
            unless = "#result.isEmpty()"
    )
    public List<SimilarProductResponseDTO> getSimilarProductsWithCache(
            String keyword,
            Pageable pageable
    ) {
        return completedDealRepository.findSimilarProducts(keyword, pageable);
    }
}