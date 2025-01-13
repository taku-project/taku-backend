package com.ani.taku_backend.marketprice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시세 검색 응답 DTO")
public class MarketPriceSearchResponseDTO {

    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    private final String keyword;

    @Schema(description = "시세 그래프 데이터")
    private final PriceGraphResponseDTO priceGraph;

    @Schema(description = "최근 일주일 통계")
    private final WeeklyStatsResponseDTO weeklyStats;

    @Schema(description = "유사 상품 목록")
    private final List<SimilarProductResponseDTO> similarProducts;

    public MarketPriceSearchResponseDTO(
            String keyword,
            PriceGraphResponseDTO priceGraph,
            WeeklyStatsResponseDTO weeklyStats,
            List<SimilarProductResponseDTO> similarProducts
    ) {
        this.keyword = keyword;
        this.priceGraph = priceGraph;
        this.weeklyStats = weeklyStats;
        this.similarProducts = similarProducts;
    }
}