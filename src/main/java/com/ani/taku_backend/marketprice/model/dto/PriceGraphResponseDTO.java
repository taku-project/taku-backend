package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
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
    @Schema(description = "시세 데이터 포인트")
    public static class PriceDataPoint {

        @Schema(description = "날짜")
        private final LocalDate date;

        @Schema(description = "상품 ID")
        private final Long productId;

        @Schema(description = "상품 제목")
        private final String title;

        @Schema(description = "등록 가격")
        private final BigDecimal registeredPrice;

        @Schema(description = "판매 가격")
        private final BigDecimal soldPrice;

        @Schema(description = "거래 건수")
        private final int dealCount;

        @JsonPOJOBuilder(withPrefix = "")
        public static class PriceDataPointBuilder {
        }
    }
}