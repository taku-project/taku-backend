package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "최근 일주일 판매 통계 DTO")
public record WeeklyStatsResDTO(
    @Schema(description = "평균 판매가", example = "81750")
    BigDecimal averagePrice,

    @Schema(description = "최고 판매가", example = "490000")
    BigDecimal highestPrice,

    @Schema(description = "최저 판매가", example = "14000")
    BigDecimal lowestPrice,

    @Schema(description = "거래 건수", example = "42")
    long totalDeals
) {
    public WeeklyStatsResDTO {
        if (averagePrice != null) {
            averagePrice = averagePrice.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static WeeklyStatsResDTO empty() {
        return new WeeklyStatsResDTO(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0L
        );
    }

    public WeeklyStatsResDTO(Double averagePrice, BigDecimal highestPrice,
                             BigDecimal lowestPrice, Long totalDeals) {
        this(
            averagePrice != null ? BigDecimal.valueOf(averagePrice) : BigDecimal.ZERO,
            highestPrice != null ? highestPrice : BigDecimal.ZERO,
            lowestPrice != null ? lowestPrice : BigDecimal.ZERO,
            totalDeals != null ? totalDeals : 0L
        );
    }
}