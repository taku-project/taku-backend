package com.ani.taku_backend.marketprice.model.entity;


import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "market_price_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketPriceStats extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private DuckuJangter product;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal registeredPrice;  // 등록가

    @Column(precision = 10, scale = 2)
    private BigDecimal soldPrice;        // 판매가

    private Double similarity;

    @Column(length = 255)
    private String searchKeyword;

    @Column(nullable = false)
    private LocalDate registeredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @Builder
    public MarketPriceStats(DuckuJangter product, String searchKeyword) {
        this.product = product;
        this.title = product.getTitle();
        this.registeredPrice = product.getPrice();
        this.status = product.getStatus();
        this.searchKeyword = searchKeyword;
        this.registeredDate = LocalDate.now();
    }

    public void updateSoldPrice(BigDecimal soldPrice) {
        this.soldPrice = soldPrice;
    }

    public void updateSimilarity(Double similarity) {
        this.similarity = similarity;
    }
}