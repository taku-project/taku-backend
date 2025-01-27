package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.service.MarketPriceStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Market Price Stats", description = "시세 통계 API")
@RestController
@RequestMapping("/api/market-price")
@RequiredArgsConstructor
public class MarketPriceStatsController {
    private final MarketPriceStatsService marketPriceStatsService;

    @Operation(summary = "시세 그래프 조회")
    @GetMapping("/graph")
    public CommonResponse<PriceGraphResponseDTO> getPriceGraph(
            @Parameter(description = "검색할 상품 키워드", required = true)
            @RequestParam String keyword,
            
            @Parameter(description = "조회 시작 날짜", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "조회 종료 날짜", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            
            @Parameter(description = "그래프 표시 옵션 (ALL, REGISTERED_PRICE_ONLY, SOLD_PRICE_ONLY)", required = false)
            @RequestParam(defaultValue = "ALL") GraphDisplayOption option) {

        return CommonResponse.ok(
                marketPriceStatsService.getPriceGraph(keyword, fromDate, toDate, option)
        );
    }

    @Operation(summary = "주간 통계 조회")
    @GetMapping("/weekly-stats")
    public CommonResponse<WeeklyStatsResponseDTO> getWeeklyStats(
            @Parameter(description = "검색할 상품 키워드", required = true)
            @RequestParam String keyword) {
        return CommonResponse.ok(marketPriceStatsService.getWeeklyStats(keyword));
    }

    @Operation(summary = "유사 상품 조회")
    @GetMapping("/similar")
    public CommonResponse<List<SimilarProductResponseDTO>> findSimilarProducts(
            @Parameter(description = "검색할 상품 키워드", required = true)
            @RequestParam String keyword,
            
            @Parameter(description = "페이지네이션 정보")
            Pageable pageable) {
        return CommonResponse.ok(
                marketPriceStatsService.findSimilarProducts(keyword, pageable)
        );
    }
}