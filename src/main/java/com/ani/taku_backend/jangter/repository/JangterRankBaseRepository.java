package com.ani.taku_backend.jangter.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.common.enums.PeriodType;
import com.ani.taku_backend.jangter.model.entity.rank.JangterRankBase;

public interface JangterRankBaseRepository extends JpaRepository<JangterRankBase, Long> {
    List<JangterRankBase> findByStartDateBetweenAndPeriodTypeOrderByTotalScoreDesc(LocalDateTime startDate, LocalDateTime endDate, PeriodType periodType);
}
