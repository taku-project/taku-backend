package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.service.MarketPriceStatsService;
import io.swagger.v3.oas.annotations.Operation;
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
            @RequestParam String keyword,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "ALL") GraphDisplayOption option) {

        return CommonResponse.ok(
                marketPriceStatsService.getPriceGraph(keyword, fromDate, toDate, option)
        );
    }

    @Operation(summary = "주간 통계 조회")
    @GetMapping("/weekly-stats")
    public CommonResponse<WeeklyStatsResponseDTO> getWeeklyStats(
            @RequestParam String keyword) {
        return CommonResponse.ok(marketPriceStatsService.getWeeklyStats(keyword));
    }

    @Operation(summary = "유사 상품 조회")
    @GetMapping("/similar")
    public CommonResponse<List<SimilarProductResponseDTO>> findSimilarProducts(
            @RequestParam String keyword,
            Pageable pageable) {
        return CommonResponse.ok(
                marketPriceStatsService.findSimilarProducts(keyword, pageable)
        );
    }
}