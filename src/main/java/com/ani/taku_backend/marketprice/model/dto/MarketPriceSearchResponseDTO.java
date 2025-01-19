package com.ani.taku_backend.marketprice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "시세 검색 응답 DTO")
public record MarketPriceSearchResponseDTO(
    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    String keyword,

    @Schema(description = "시세 그래프 데이터")
    PriceGraphResponseDTO priceGraph,

    @Schema(description = "최근 일주일 통계")
    WeeklyStatsResponseDTO weeklyStats,

    @Schema(description = "유사 상품 목록")
    List<SimilarProductResponseDTO> similarProducts
) {}