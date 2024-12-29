package com.ani.taku_backend.marketprice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "최근 일주일 판매 통계 DTO")
public class WeeklyStatsResponseDTO {
    @Schema(description = "평균 판매가", example = "81750")
    private final BigDecimal averagePrice;

    @Schema(description = "최고 판매가", example = "490000")
    private final BigDecimal highestPrice;

    @Schema(description = "최저 판매가", example = "14000")
    private final BigDecimal lowestPrice;

    @Schema(description = "거래 건수", example = "42")
    private final int totalDeals;
}