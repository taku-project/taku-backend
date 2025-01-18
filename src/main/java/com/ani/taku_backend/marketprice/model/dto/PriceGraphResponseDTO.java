package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.common.util.TypeIdResolverForDevTools;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonTypeIdResolver(TypeIdResolverForDevTools.class)
@Schema(description = "시세 그래프 응답 DTO")
public record PriceGraphResponseDTO(
        @Schema(description = "날짜별 데이터")
        List<PriceDataPoint> dataPoints
) {
    public static PriceGraphResponseDTO empty() {
        return new PriceGraphResponseDTO(Collections.emptyList());
    }

    @Builder
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    @JsonTypeIdResolver(TypeIdResolverForDevTools.class)
    @Schema(description = "시세 데이터 포인트")
    public record PriceDataPoint(
            @Schema(description = "날짜", example = "2024-01-01")
            LocalDate date,

            @Schema(description = "상품 ID", example = "1")
            Long productId,

            @Schema(description = "상품 제목", example = "원피스 루피 피규어")
            String title,

            @Schema(description = "등록 가격", example = "50000")
            BigDecimal registeredPrice,

            @Schema(description = "판매 가격", example = "45000")
            BigDecimal soldPrice,

            @Schema(description = "거래 건수", example = "3")
            int dealCount


    ) {}
}