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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam("keyword") String keyword,

            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", required = true)
            @RequestParam("startDate") String startDate,

            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", required = true)
            @RequestParam("endDate") String endDate,

            @Parameter(description = "그래프 표시 옵션 (기본값: ALL)", required = false)
            @RequestParam(defaultValue = "ALL", name = "displayOption") GraphDisplayOption displayOption,

            @Parameter(description = "요청할 페이지 번호 (기본값: 0)", example = "0", required = false)
            @RequestParam(defaultValue = "0", name = "page") int page, // 페이지 번호

            @Parameter(description = "한 페이지당 데이터 개수 (기본값: 5)", example = "5", required = false)
            @RequestParam(defaultValue = "5", name = "size") int size, // 페이지 크기

            @Parameter(description = "정렬 조건 (기본값: id,asc)", example = "id,asc", required = false)
            @RequestParam(defaultValue = "id,asc", name = "sort") String sort // 정렬 조건
    ) {
        try {
            log.debug("시세 조회 요청 - keyword: {}, startDate: {}, endDate: {}, displayOption: {}, page: {}, size: {}, sort: {}",
                    keyword, startDate, endDate, displayOption, page, size, sort);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);


            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

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