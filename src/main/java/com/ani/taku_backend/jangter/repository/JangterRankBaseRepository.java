package com.ani.taku_backend.jangter.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.common.enums.PeriodType;
import com.ani.taku_backend.jangter.model.entity.rank.JangterRankBase;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JangterRankBaseRepository extends JpaRepository<JangterRankBase, Long> {
    List<JangterRankBase> findByStartDateBetweenAndPeriodTypeOrderByTotalScoreDesc(LocalDateTime startDate, LocalDateTime endDate, PeriodType periodType);


    // 특정 기간의 랭킹 조회 (장터 데이터 fetch join)
    @Query("""
            SELECT DISTINCT jrb FROM JangterRankBase jrb
            JOIN FETCH jrb.duckuJangter dj
            LEFT JOIN FETCH dj.jangterImages ji
            LEFT JOIN FETCH ji.image img
            LEFT JOIN FETCH dj.user u
            WHERE jrb.periodType = :periodType
                AND jrb.periodKey = :periodKey
            ORDER BY jrb.periodKey DESC, jrb.rankIdx ASC
            """)
    List<JangterRankBase> findRanksByPeriodTypeAndDateRange(
        @Param("periodType") PeriodType periodType,
        @Param("periodKey") String periodKey
    );


}
