package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.service.CompletedDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "시세 조회 API", description = "상품 시세 조회 관련 API")
@RestController
@RequestMapping("/api/market-price")
@RequiredArgsConstructor
public class CompletedDealController {
    private final CompletedDealService completedDealService;

    @Operation(summary = "시세 조회", description = "키워드로 상품 시세를 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<MarketPriceSearchResponseDTO> searchMarketPrice(
            @Parameter(description = "검색 키워드", example = "원피스 루피 피규어")
            @RequestParam String keyword,

            @Parameter(description = "조회 시작일", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "조회 종료일", example = "2024-03-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "그래프 표시 옵션")
            @RequestParam(required = false, defaultValue = "ALL")
            GraphDisplayOption displayOption,

            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 5) Pageable pageable
    ) {
        /*기본 날짜 범위 설정
          fromDate가 null이면 → 현재 날짜에서 3개월 전
          toDate가 null이면 → 현재 날짜
         */

        LocalDate startDate;
        if (fromDate != null) {
            startDate = fromDate;
        } else {
            startDate = LocalDate.now().minusMonths(3);
        }

        LocalDate endDate;
        if (toDate != null) {
            endDate = toDate;
        } else {
            endDate = LocalDate.now();
        }

        return ResponseEntity.ok(
                completedDealService.searchMarketPrice(
                        keyword, startDate, endDate, displayOption, pageable
                )
        );   // 요거 이상함
    }
}