package com.ani.taku_backend.marketprice.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "completed_deals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompletedDeal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "search_keywords", length = 100)
    @Comment("상품 키워드")
    private String searchKeywords;

    @Column(name = "similarity_score")
    private Double similarity;

    public void updateSimilarityScore(double score) {
        this.similarity = score;
    }
}