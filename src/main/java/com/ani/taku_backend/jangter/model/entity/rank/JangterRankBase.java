package com.ani.taku_backend.jangter.model.entity.rank;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.PeriodType;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.ProductScoreDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

@Builder
@Entity
@Table(name = "jangter_rank_base")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"jangterRankStats" , "duckuJangter"})
public class JangterRankBase extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "rank_idx")
    private int rankIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private DuckuJangter duckuJangter;

    @OneToMany(fetch = FetchType.LAZY , mappedBy = "jangterRankBase" , orphanRemoval = true)
    private List<JangterRankStats> jangterRankStats;

    @Column(name = "period_type", length = 15)
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "period_key")
    private String periodKey;

    public static JangterRankBase create(ProductScoreDTO productScoreDTO) {
        return JangterRankBase.builder()
            .totalScore(productScoreDTO.getTotalScore())
            .status(StatusType.ACTIVE)
            .rankIdx(productScoreDTO.getRank())
            .duckuJangter(DuckuJangter.reference(productScoreDTO.getProductId()))
            .build();
    }

    public void addRankStats(List<JangterRankStats> stats) {
        this.jangterRankStats = stats;
    }
}
