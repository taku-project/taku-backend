package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;

import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import java.time.LocalDate;

public interface MarketPriceStatsQueryRepository {
    PriceGraphResponseDTO getPriceGraph(String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option);

    WeeklyStatsResponseDTO getWeeklyStats(String keyword);
}