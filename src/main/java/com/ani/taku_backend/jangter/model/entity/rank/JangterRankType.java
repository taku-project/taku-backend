package com.ani.taku_backend.jangter.model.entity.rank;

import java.math.BigDecimal;
import java.util.List;

import com.ani.taku_backend.common.enums.RankType;
import com.ani.taku_backend.common.enums.StatusType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "jangter_rank_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JangterRankType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type", length = 15)
    @Enumerated(EnumType.STRING)
    private RankType type;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "weight")
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StatusType status;

    @OneToMany(fetch = FetchType.LAZY , mappedBy = "jangterRankType")
    private List<JangterRankStats> jangterRankStats;
}