package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MarketPriceStatsRepository extends JpaRepository<MarketPriceStats, Long>, MarketPriceStatsQueryRepository {
    List<MarketPriceStats> findByRegisteredDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<MarketPriceStats> findFirstByProductOrderByRegisteredDateDesc(DuckuJangter product);
    List<MarketPriceStats> findByTitleContaining(String keyword);

    @Query("""
            SELECT new com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO(
                AVG(m.registeredPrice),
                MAX(m.registeredPrice),
                MIN(m.registeredPrice),
                COUNT(m)
            )
            FROM MarketPriceStats m
            WHERE m.title LIKE %:keyword%
            AND m.registeredDate >= :startDate
            AND m.registeredDate <= :endDate
            """)
    WeeklyStatsResponseDTO getWeeklyStats(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}