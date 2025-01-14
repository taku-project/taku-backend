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
    private final long totalDeals;


    public WeeklyStatsResponseDTO(Double averagePrice, BigDecimal highestPrice,
                                  BigDecimal lowestPrice, Long totalDeals) {
        this.averagePrice = averagePrice != null ? BigDecimal.valueOf(averagePrice) : BigDecimal.ZERO;
        this.highestPrice = highestPrice != null ? highestPrice : BigDecimal.ZERO;
        this.lowestPrice = lowestPrice != null ? lowestPrice : BigDecimal.ZERO;
        this.totalDeals = totalDeals != null ? totalDeals : 0L;
    }

    @Builder
    public WeeklyStatsResponseDTO(BigDecimal averagePrice, BigDecimal highestPrice,
                                  BigDecimal lowestPrice, long totalDeals) {
        this.averagePrice = averagePrice != null ? averagePrice : BigDecimal.ZERO;
        this.highestPrice = highestPrice != null ? highestPrice : BigDecimal.ZERO;
        this.lowestPrice = lowestPrice != null ? lowestPrice : BigDecimal.ZERO;
        this.totalDeals = totalDeals;
    }

    public static WeeklyStatsResponseDTO empty() {
        return WeeklyStatsResponseDTO.builder()
                .averagePrice(BigDecimal.ZERO)
                .highestPrice(BigDecimal.ZERO)
                .lowestPrice(BigDecimal.ZERO)
                .totalDeals(0L)
                .build();
    }
}