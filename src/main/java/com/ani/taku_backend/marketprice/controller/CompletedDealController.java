package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphRequestDTO;
import com.ani.taku_backend.marketprice.service.CompletedDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Slf4j
@Tag(name = "Market Price", description = "시세 조회 API")
@RestController
@RequestMapping("/api/market-price")
@RequiredArgsConstructor
public class CompletedDealController {

    private final CompletedDealService completedDealService;

    @Operation(summary = "시세 조회", description = "키워드로 상품 시세를 조회합니다.")
    @GetMapping("/search")
    public CommonResponse<MarketPriceSearchResponseDTO> searchMarketPrice(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword,

            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", required = true)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @RequestParam LocalDate startDate,

            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", required = true)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @RequestParam LocalDate endDate,

            @Parameter(description = "그래프 표시 옵션 (기본값: ALL)", required = false)
            @RequestParam(defaultValue = "ALL") GraphDisplayOption displayOption,

            @ParameterObject 
            @PageableDefault(size = 5, sort = "id") Pageable pageable
    ) {
        try {
            var requestDTO = new PriceGraphRequestDTO(keyword, startDate, endDate, displayOption);
            MarketPriceSearchResponseDTO response = completedDealService.searchMarketPrice(requestDTO, pageable);
            log.debug("시세 조회 응답: {}", response);
            return CommonResponse.ok(response);
        } catch (Exception e) {
            log.error("시세 조회 중 오류 발생", e);
            throw e;
        }
    }
}