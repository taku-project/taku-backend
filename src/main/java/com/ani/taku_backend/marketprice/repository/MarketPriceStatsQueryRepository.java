package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;

import java.time.LocalDate;

public interface MarketPriceStatsQueryRepository {
    PriceGraphResponseDTO getPriceGraph(String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option);
}