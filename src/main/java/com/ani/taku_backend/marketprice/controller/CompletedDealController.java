package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphRequestDTO;
import com.ani.taku_backend.marketprice.service.CompletedDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public CommonResponse<MarketPriceSearchResponseDTO> searchMarketPrice(
            @Valid @ModelAttribute PriceGraphRequestDTO requestDTO,
            @RequestParam(required = false, defaultValue = "ALL") GraphDisplayOption displayOption,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        MarketPriceSearchResponseDTO response = completedDealService.searchMarketPrice(requestDTO, pageable);
        return CommonResponse.ok(response);
    }
}