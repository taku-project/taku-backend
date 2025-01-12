package com.ani.taku_backend.marketprice.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphRequestDTO;
import com.ani.taku_backend.marketprice.service.CompletedDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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
            @Valid @ModelAttribute PriceGraphRequestDTO requestDTO,
            @RequestParam(required = false, defaultValue = "ALL") GraphDisplayOption displayOption,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        try {
            log.debug("시세 조회 요청 - requestDTO: {}, displayOption: {}, pageable: {}",
                    requestDTO, displayOption, pageable);

            requestDTO.setDisplayOption(displayOption);

            MarketPriceSearchResponseDTO response = completedDealService.searchMarketPrice(requestDTO, pageable);

            log.debug("시세 조회 응답: {}", response);

            return CommonResponse.ok(response);

        } catch (Exception e) {
            log.error("시세 조회 중 오류 발생", e);
            throw e;
        }
    }
}