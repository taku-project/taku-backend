package com.ani.taku_backend.marketprice.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.marketprice.config.DateConfig;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.*;
import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompletedDealService {
    private final CompletedDealRepository completedDealRepository;
    private final ExtractKeywordService extractKeywordService;
    private final DateConfig dateConfig;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    @Transactional(readOnly = true)
    @Cacheable(value = "marketPrice", key = "#requestDTO.keyword + #requestDTO.fromDate + #requestDTO.toDate + #requestDTO.displayOption")
    public MarketPriceSearchResponseDTO searchMarketPrice(PriceGraphRequestDTO requestDTO, Pageable pageable) {
        try {
            // 날짜 처리 - null이 될 수 없도록 보장 수정
            LocalDate startDate = Optional.ofNullable(requestDTO.getFromDate())
                    .orElseGet(dateConfig::getDefaultStartDate);
            LocalDate endDate = Optional.ofNullable(requestDTO.getToDate())
                    .orElseGet(dateConfig::getDefaultEndDate);

            validateDateRange(startDate, endDate);

            String processedKeyword = processKeyword(requestDTO.getKeyword());

            return MarketPriceSearchResponseDTO.builder()
                    .keyword(requestDTO.getKeyword())
                    .priceGraph(getPriceGraphData(processedKeyword, startDate, endDate, requestDTO.getDisplayOption()))
                    .weeklyStats(getWeeklyStats(processedKeyword))
                    .similarProducts(getSimilarProducts(processedKeyword, pageable))
                    .build();

        } catch (Exception e) {
            log.error("시세 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }
    }

    private String processKeyword(String keyword) {
        List<String> extractedKeywords = extractKeywordService.extractKeywords(keyword);
        if (extractedKeywords.isEmpty()) {
            throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return String.join(" ", extractedKeywords);
    }

    private PriceGraphResponseDTO getPriceGraphData(String keyword, LocalDate startDate, LocalDate endDate, GraphDisplayOption option) {
        PriceGraphResponseDTO data = completedDealRepository.getPriceGraph(keyword, startDate, endDate, option);
        if (data == null) {
            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }
        return data;
    }
    private WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
        WeeklyStatsResponseDTO stats = completedDealRepository.getWeeklyStats(keyword);
        if (stats == null) {
            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }
        return stats;
    }

    private List<SimilarProductResponseDTO> getSimilarProducts(String keyword, Pageable pageable) {
        List<SimilarProductResponseDTO> products = completedDealRepository.findSimilarProducts(keyword, pageable);
        if (products.isEmpty()) {
            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }
        return products;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new DuckwhoException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}