package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MarketPriceStatsRepository extends JpaRepository<MarketPriceStats, Long>, MarketPriceStatsQueryRepository {
    List<MarketPriceStats> findByRegisteredDateBetween(LocalDate startDate, LocalDate endDate);
}