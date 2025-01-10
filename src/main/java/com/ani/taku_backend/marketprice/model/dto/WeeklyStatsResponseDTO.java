package com.ani.taku_backend.marketprice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
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

    public WeeklyStatsResponseDTO(
            Double averagePrice,
            BigDecimal highestPrice,
            BigDecimal lowestPrice,
            long totalDeals
    ) {
        if (averagePrice != null) {
            this.averagePrice = BigDecimal.valueOf(averagePrice);
        } else {
            this.averagePrice = BigDecimal.ZERO;
        }
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.totalDeals = totalDeals;
    }

    /**
     * 빈 통계 데이터를 생성하는 팩토리 메서드
     * @return 모든 값이 0인 WeeklyStatsResponseDTO
     */
    public static WeeklyStatsResponseDTO empty() {
        return new WeeklyStatsResponseDTO(
                0.0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L
        );
    }
}