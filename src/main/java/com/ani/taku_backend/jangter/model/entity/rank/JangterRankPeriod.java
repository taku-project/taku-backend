package com.ani.taku_backend.jangter.model.entity.rank;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
@Builder
@Entity
@Table(name = "jangter_rank_periods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JangterRankPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "period_type" , length = 15)
    private String periodType;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "period_key")
    private String periodKey;

    @OneToMany(fetch = FetchType.LAZY , mappedBy = "jangterRankPeriod")
    private List<JangterRankStats> jangterRankStats;
}
