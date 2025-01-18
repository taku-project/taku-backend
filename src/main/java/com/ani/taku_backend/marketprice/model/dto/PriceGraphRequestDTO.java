package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.common.util.TypeIdResolverForDevTools;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonTypeIdResolver(TypeIdResolverForDevTools.class)
@Schema(description = "시세 그래프 요청 DTO")
public record PriceGraphRequestDTO(
        @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
        @NotBlank(message = "키워드는 필수입니다")
        String keyword,

        @Schema(description = "조회 시작일", example = "2024-01-01")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate fromDate,

        @Schema(description = "조회 종료일", example = "2024-01-31")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate toDate,

        @Schema(description = "그래프 표시 옵션", example = "ALL")
        GraphDisplayOption displayOption
) {
    @Override
    public String toString() {
        return "PriceGraphRequestDTO{" +
                "keyword='" + keyword + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", displayOption=" + displayOption +
                '}';
    }
}