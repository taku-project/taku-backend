package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MarketPriceSearchResponseDTO {

    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    private final String keyword;

    @Schema(description = "시세 그래프 데이터")
    private final PriceGraphResponseDTO priceGraph;

    @Schema(description = "최근 일주일 통계")
    private final WeeklyStatsResponseDTO weeklyStats;

    @Schema(description = "유사 상품 목록")
    private final List<SimilarProductResponseDTO> similarProducts;

    @JsonCreator
    @Builder
    public MarketPriceSearchResponseDTO(
            @JsonProperty("keyword") String keyword,
            @JsonProperty("priceGraph") PriceGraphResponseDTO priceGraph,
            @JsonProperty("weeklyStats") WeeklyStatsResponseDTO weeklyStats,
            @JsonProperty("similarProducts") List<SimilarProductResponseDTO> similarProducts
    ) {
        this.keyword = keyword;
        this.priceGraph = priceGraph;
        this.weeklyStats = weeklyStats;
        this.similarProducts = similarProducts;
    }
}