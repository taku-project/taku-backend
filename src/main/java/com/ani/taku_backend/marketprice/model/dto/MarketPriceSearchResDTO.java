package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "시세 검색 응답 DTO")
public record MarketPriceSearchResDTO(
    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    String keyword,

    @Schema(description = "시세 그래프 데이터")
    PriceGraphResDTO priceGraph,

    @Schema(description = "최근 일주일 통계")
    WeeklyStatsResDTO weeklyStats,

    @Schema(description = "유사 상품 목록")
    List<SimilarProductResDTO> similarProducts,

    @Schema(description = "판매 완료 상품 평균 판매 가격", example = "45000")
    BigDecimal averageSoldPrice
) {}