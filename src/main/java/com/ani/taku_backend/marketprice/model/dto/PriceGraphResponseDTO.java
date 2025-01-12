package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 시세 그래프 응답 DTO
 */
@Getter
@Builder
@JsonDeserialize(builder = PriceGraphResponseDTO.PriceGraphResponseDTOBuilder.class)
@Schema(description = "시세 그래프 응답 DTO")
public class PriceGraphResponseDTO {

    @Schema(description = "날짜별 데이터")
    private final List<PriceDataPoint> dataPoints;

    public static PriceGraphResponseDTO empty() {
        return PriceGraphResponseDTO.builder()
                .dataPoints(Collections.emptyList())
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class PriceGraphResponseDTOBuilder {
    }

    @Getter
    @Builder
    @JsonDeserialize(builder = PriceDataPoint.PriceDataPointBuilder.class)
    public static class PriceDataPoint {

        private final LocalDate date;            // 날짜
        private final BigDecimal registeredPrice; // 등록 가격
        private final BigDecimal soldPrice;       // 판매 가격
        private final int dealCount;             // 거래 건수

        @JsonPOJOBuilder(withPrefix = "")
        public static class PriceDataPointBuilder {
        }
    }
}