package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "최근 일주일 판매 통계 DTO")
public record WeeklyStatsResponseDTO(
    @Schema(description = "평균 판매가", example = "81750")
    BigDecimal averagePrice,

    @Schema(description = "최고 판매가", example = "490000")
    BigDecimal highestPrice,

    @Schema(description = "최저 판매가", example = "14000")
    BigDecimal lowestPrice,

    @Schema(description = "거래 건수", example = "42")
    long totalDeals
) {
    public static WeeklyStatsResponseDTO empty() {
        return new WeeklyStatsResponseDTO(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0L
        );
    }

    public WeeklyStatsResponseDTO(Double averagePrice, BigDecimal highestPrice, 
            BigDecimal lowestPrice, Long totalDeals) {
        this(
            averagePrice != null ? BigDecimal.valueOf(averagePrice) : BigDecimal.ZERO,
            highestPrice != null ? highestPrice : BigDecimal.ZERO,
            lowestPrice != null ? lowestPrice : BigDecimal.ZERO,
            totalDeals != null ? totalDeals : 0L
        );
    }
}