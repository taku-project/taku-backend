package com.ani.taku_backend.marketprice.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "market_price_stats")
public class MarketPriceStats extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long statsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private DuckuJangter product;

    @NotNull
    @Column(length = 255, nullable = false)
    private String title;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal registeredPrice;  // 등록가

    @Column(precision = 10, scale = 2)
    private BigDecimal soldPrice;        // 판매가

    @NotNull
    @Column(nullable = false)
    private LocalDate registeredDate;    // 등록일

    @Builder
    public MarketPriceStats(
            DuckuJangter product,
            String title,
            BigDecimal registeredPrice,
            BigDecimal soldPrice,
            LocalDate registeredDate
    ) {
        this.product = product;
        this.title = title;
        this.registeredPrice = registeredPrice;
        this.soldPrice = soldPrice;
        this.registeredDate = registeredDate;
    }

    public void updateSoldPrice(BigDecimal soldPrice) {
        this.soldPrice = soldPrice;
    }
}