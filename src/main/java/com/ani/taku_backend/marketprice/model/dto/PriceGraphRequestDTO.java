package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시세 그래프 조회 요청 DTO")
public class PriceGraphRequestDTO {
    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    private final String keyword;

    @Schema(description = "조회 시작일", example = "2024-01-01")
    private final LocalDate fromDate;

    @Schema(description = "조회 종료일", example = "2024-03-31")
    private final LocalDate toDate;

    @Schema(description = "그래프 표시 옵션")
    private final GraphDisplayOption displayOption;
}