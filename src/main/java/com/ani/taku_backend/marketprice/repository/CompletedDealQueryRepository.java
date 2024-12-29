package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CompletedDealQueryRepository {
    // 시세 그래프 데이터 조회
    PriceGraphResponseDTO getPriceGraph(String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option);

    // 최근 일주일 통계 조회
    WeeklyStatsResponseDTO getWeeklyStats(String keyword);

    // 유사 상품 조회
    List<SimilarProductResponseDTO> findSimilarProducts(String keyword, Pageable pageable);
}