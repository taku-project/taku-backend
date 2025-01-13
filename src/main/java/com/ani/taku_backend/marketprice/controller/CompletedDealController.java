package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphRequestDTO;
import com.ani.taku_backend.marketprice.service.CompletedDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Tag(name = "시세 조회 API", description = "상품 시세 조회 관련 API")
@RestController
@RequestMapping("/api/market-price")
@RequiredArgsConstructor
public class CompletedDealController {

    private final CompletedDealService completedDealService;

    @Operation(summary = "시세 조회", description = "키워드로 상품 시세를 조회합니다.")
    @GetMapping("/search")
    public CommonResponse<MarketPriceSearchResponseDTO> searchMarketPrice(
            @RequestParam String keyword,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "ALL") GraphDisplayOption displayOption,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        try {
            log.debug("시세 조회 요청 - keyword: {}, startDate: {}, endDate: {}, displayOption: {}, pageable: {}",
                    keyword, startDate, endDate, displayOption, pageable);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

            var requestDTO = new PriceGraphRequestDTO(keyword, parsedStartDate, parsedEndDate, displayOption);

            MarketPriceSearchResponseDTO response = completedDealService.searchMarketPrice(requestDTO, pageable);

            log.debug("시세 조회 응답: {}", response);

            return CommonResponse.ok(response);

        } catch (Exception e) {
            log.error("시세 조회 중 오류 발생", e);
            throw e;
        }
    }
}