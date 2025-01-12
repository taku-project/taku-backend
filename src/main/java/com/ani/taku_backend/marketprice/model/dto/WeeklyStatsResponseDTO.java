package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

/**
 * 최근 일주일 판매 통계 DTO
 */
@Getter
@Builder
@JsonDeserialize(builder = WeeklyStatsResponseDTO.WeeklyStatsResponseDTOBuilder.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
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

    public WeeklyStatsResponseDTO(Double avg, BigDecimal max, BigDecimal min, Long count) {
        this(
                (avg != null) ? BigDecimal.valueOf(avg) : BigDecimal.ZERO, // averagePrice
                (max != null) ? max : BigDecimal.ZERO,                     // highestPrice
                (min != null) ? min : BigDecimal.ZERO,                     // lowestPrice
                (count != null) ? count : 0L                               // totalDeals
        );
    }

    public static WeeklyStatsResponseDTO empty() {
        return WeeklyStatsResponseDTO.builder()
                .averagePrice(BigDecimal.ZERO)
                .highestPrice(BigDecimal.ZERO)
                .lowestPrice(BigDecimal.ZERO)
                .totalDeals(0L)
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class WeeklyStatsResponseDTOBuilder {

        public WeeklyStatsResponseDTO build() {
            BigDecimal finalAvgPrice = (averagePrice == null) ? BigDecimal.ZERO : averagePrice;

            return new WeeklyStatsResponseDTO(
                    finalAvgPrice,
                    (highestPrice == null) ? BigDecimal.ZERO : highestPrice,
                    (lowestPrice == null) ? BigDecimal.ZERO : lowestPrice,
                    totalDeals
            );
        }
    }

    private WeeklyStatsResponseDTO(
            BigDecimal averagePrice,
            BigDecimal highestPrice,
            BigDecimal lowestPrice,
            long totalDeals
    ) {
        this.averagePrice = averagePrice;
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.totalDeals = totalDeals;
    }
}