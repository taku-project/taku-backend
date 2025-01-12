package com.ani.taku_backend.marketprice.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "completed_deal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompletedDeal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private DuckuJangter product;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stats_id")
    private MarketPriceStats marketPriceStats;

    @NotNull
    @Column(length = 255, nullable = false)
    private String title;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;      // 최종 거래가

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "search_keywords", length = 255)
    private String searchKeywords;

    @Builder
    public CompletedDeal(
            DuckuJangter product,
            MarketPriceStats marketPriceStats,
            String title,
            BigDecimal price,
            String categoryName,
            String searchKeywords
    ) {
        this.product = product;
        this.marketPriceStats = marketPriceStats;
        this.title = title;
        this.price = price;
        this.categoryName = categoryName;
        this.searchKeywords = searchKeywords;
    }
}