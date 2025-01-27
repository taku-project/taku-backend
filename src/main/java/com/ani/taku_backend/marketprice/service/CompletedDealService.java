//package com.ani.taku_backend.marketprice.service;
//
//import com.ani.taku_backend.common.exception.DuckwhoException;
//import com.ani.taku_backend.common.exception.ErrorCode;
//import com.ani.taku_backend.common.service.ExtractKeywordService;
//import com.ani.taku_backend.jangter.model.entity.ItemCategories;
//import com.ani.taku_backend.marketprice.config.DateConfig;
//import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
//import com.ani.taku_backend.marketprice.model.dto.*;
//import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
//import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CompletedDealService {
//    private final CompletedDealRepository completedDealRepository;
//    private final ExtractKeywordService extractKeywordService;
//    private final DateConfig dateConfig;
//
//    public MarketPriceSearchResponseDTO searchMarketPrice(PriceGraphRequestDTO requestDTO, Pageable pageable) {
//        try {
//            if (requestDTO == null || requestDTO.getKeyword() == null) {
//                throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
//            }
//
//            LocalDate startDate = Optional.ofNullable(requestDTO.getFromDate())
//                    .orElseGet(dateConfig::getDefaultStartDate);
//            LocalDate endDate = Optional.ofNullable(requestDTO.getToDate())
//                    .orElseGet(dateConfig::getDefaultEndDate);
//
//            validateDateRange(startDate, endDate);
//
//            String processedKeyword = processKeyword(requestDTO.getKeyword());
//
//            return MarketPriceSearchResponseDTO.builder()
//                    .keyword(requestDTO.getKeyword())
//                    .priceGraph(getPriceGraphData(processedKeyword, startDate, endDate, requestDTO.getDisplayOption()))
//                    .weeklyStats(getWeeklyStats(processedKeyword))
//                    .similarProducts(getSimilarProducts(processedKeyword, pageable))
//                    .build();
//
//        } catch (Exception e) {
//            log.error("시세 조회 중 오류 발생: {}", e.getMessage(), e);
//            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
//        }
//    }
//
//    private String processKeyword(String keyword) {
//        List<String> extractedKeywords = extractKeywordService.extractKeywords(keyword);
//        if (extractedKeywords.isEmpty()) {
//            throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
//        }
//        return String.join(" ", extractedKeywords);
//    }
//
//    private PriceGraphResponseDTO getPriceGraphData(String keyword, LocalDate startDate, LocalDate endDate, GraphDisplayOption option) {
//        PriceGraphResponseDTO data = completedDealRepository.getPriceGraph(keyword, startDate, endDate, option);
//        if (data == null) {
//            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
//        }
//        return data;
//    }
//
//    WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
//        WeeklyStatsResponseDTO stats = completedDealRepository.getWeeklyStats(keyword);
//        if (stats == null) {
//            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
//        }
//        return stats;
//    }
//
//    List<SimilarProductResponseDTO> getSimilarProducts(String keyword, Pageable pageable) {
//        List<SimilarProductResponseDTO> products = completedDealRepository.findSimilarProducts(keyword, pageable);
//        if (products.isEmpty()) {
//            throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
//        }
//        return products;
//    }
//
//    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
//        if (startDate.isAfter(endDate)) {
//            throw new DuckwhoException(ErrorCode.INVALID_DATE_RANGE);
//        }
//    }
//
//    public List<CompletedDeal> findRecentDealsInCategory(ItemCategories category, LocalDateTime after) {
//        return completedDealRepository.findByCategoryNameAndCreatedAtAfterOrderByCreatedAtDesc(
//                category.getName(),
//                after
//        );
//    }
//}

package com.ani.taku_backend.marketprice.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.marketprice.config.DateConfig;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.*;
import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletedDealService {
    private final CompletedDealRepository completedDealRepository;
    private final ExtractKeywordService extractKeywordService;
    private final DateConfig dateConfig;

    @Transactional(readOnly = true)
    @Cacheable(value = "marketPrice",
            key = "#requestDTO != null ? " +
                    "(#requestDTO.keyword != null ? #requestDTO.keyword : '') + " +
                    "(#requestDTO.fromDate != null ? #requestDTO.fromDate : '') + " +
                    "(#requestDTO.toDate != null ? #requestDTO.toDate : '') + " +
                    "(#requestDTO.displayOption != null ? #requestDTO.displayOption : '') " +
                    ": ''")
    public MarketPriceSearchResponseDTO searchMarketPrice(PriceGraphRequestDTO requestDTO, Pageable pageable) {
        try {
            if (requestDTO == null || requestDTO.keyword() == null) {
                throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
            }

            LocalDate startDate = Optional.ofNullable(requestDTO.fromDate())
                    .orElseGet(dateConfig::getDefaultStartDate);
            LocalDate endDate = Optional.ofNullable(requestDTO.toDate())
                    .orElseGet(dateConfig::getDefaultEndDate);

            validateDateRange(startDate, endDate);

            String processedKeyword = processKeyword(requestDTO.keyword());

            return MarketPriceSearchResponseDTO.builder()
                    .keyword(requestDTO.keyword())
                    .priceGraph(getPriceGraphData(processedKeyword, startDate, endDate, requestDTO.displayOption()))
                    .weeklyStats(getWeeklyStats(processedKeyword))
                    .similarProducts(getSimilarProducts(processedKeyword, pageable))
                    .build();

        }  catch (Exception e) {
        log.error("시세 조회 중 오류 발생: {}", e.getMessage(), e);

        if (e instanceof DuckwhoException) {
            throw e;
        }
        throw new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
    }
    }

    private String processKeyword(String keyword) {
        List<String> extractedKeywords = extractKeywordService.extractKeywords(keyword);
        if (extractedKeywords.isEmpty()) {
            log.warn("키워드 추출 실패: {}", keyword);
            return keyword;
        }
        return String.join(" ", extractedKeywords);
    }

    private PriceGraphResponseDTO getPriceGraphData(String keyword, LocalDate startDate, LocalDate endDate, GraphDisplayOption option) {
        try {
            PriceGraphResponseDTO data = completedDealRepository.getPriceGraph(keyword, startDate, endDate, option);
            if (data == null || data.dataPoints().isEmpty()) {
                log.warn("키워드 '{}' 에 대한 가격 데이터를 찾을 수 없습니다.", keyword);
                return PriceGraphResponseDTO.empty();
            }
            return data;
        } catch (Exception e) {
            log.error("가격 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            return PriceGraphResponseDTO.empty();
        }
    }

    WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
        try {
            WeeklyStatsResponseDTO stats = completedDealRepository.getWeeklyStats(keyword);
            if (stats == null) {
                log.warn("키워드 '{}' 에 대한 주간 통계를 찾을 수 없습니다.", keyword);
                return WeeklyStatsResponseDTO.empty();
            }
            return stats;
        } catch (Exception e) {
            log.error("주간 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return WeeklyStatsResponseDTO.empty();
        }
    }

    List<SimilarProductResponseDTO> getSimilarProducts(String keyword, Pageable pageable) {
        try {
            List<SimilarProductResponseDTO> products = completedDealRepository.findSimilarProducts(keyword, pageable);
            if (products.isEmpty()) {
                log.warn("키워드 '{}' 에 대한 유사 상품을 찾을 수 없습니다.", keyword);
                return List.of();
            }
            return products;
        } catch (Exception e) {
            log.error("유사 상품 조회 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new DuckwhoException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    public List<CompletedDeal> findRecentDealsInCategory(ItemCategories category, LocalDateTime after) {
        return completedDealRepository.findByCategoryNameAndCreatedAtAfterOrderByCreatedAtDesc(
                category.getName(),
                after
        );
    }

    public List<CompletedDeal> findAllDeals() {
        return completedDealRepository.findAll();
    }
}