package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MarketPriceStatsRepository extends JpaRepository<MarketPriceStats, Long>, MarketPriceStatsQueryRepository {
    List<MarketPriceStats> findByRegisteredDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<MarketPriceStats> findFirstByProductOrderByRegisteredDateDesc(DuckuJangter product);
    WeeklyStatsResponseDTO getWeeklyStats(String keyword);
}