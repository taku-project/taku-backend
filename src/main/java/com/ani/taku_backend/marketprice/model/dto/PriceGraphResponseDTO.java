package com.ani.taku_backend.marketprice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시세 그래프 응답 DTO")
public class PriceGraphResponseDTO {
    @Schema(description = "날짜별 데이터")
    private final List<PriceDataPoint> dataPoints;

    @Getter
    @Builder
    public static class PriceDataPoint {
        @Schema(description = "날짜")
        private final LocalDate date;

        @Schema(description = "등록 가격", example = "21000")
        private final BigDecimal registeredPrice;

        @Schema(description = "판매 가격", example = "19500")
        private final BigDecimal soldPrice;

        @Schema(description = "거래량", example = "5")
        private final int dealCount;
    }
}