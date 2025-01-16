package com.ani.taku_backend.marketprice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시세 그래프 응답 DTO")
public class PriceGraphResponseDTO {

    @Schema(description = "날짜별 데이터")
    private final List<PriceDataPoint> dataPoints;

    public static PriceGraphResponseDTO empty() {
        return PriceGraphResponseDTO.builder()
                .dataPoints(Collections.emptyList())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "시세 데이터 포인트")
    public static class PriceDataPoint {

        @Schema(description = "날짜", example = "2024-01-01")
        private final LocalDate date;

        @Schema(description = "상품 ID", example = "1")
        private final Long productId;

        @Schema(description = "상품 제목", example = "원피스 루피 피규어")
        private final String title;

        @Schema(description = "등록 가격", example = "50000")
        private final BigDecimal registeredPrice;

        @Schema(description = "판매 가격", example = "45000")
        private final BigDecimal soldPrice;

        @Schema(description = "거래 건수", example = "3")
        private final int dealCount;
    }
}