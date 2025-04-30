package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.dto.PriceGraphResDTO;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;

import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResDTO;
import java.time.LocalDate;

public interface MarketPriceStatsQueryRepository {
    PriceGraphResDTO getPriceGraph(String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option);

    WeeklyStatsResDTO getWeeklyStats(String keyword);
}