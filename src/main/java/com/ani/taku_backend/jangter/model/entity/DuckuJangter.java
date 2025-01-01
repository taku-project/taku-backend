package com.ani.taku_backend.jangter.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Builder
@Entity
@Table(name = "ducku_jangter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DuckuJangter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategories itemCategory;

    @Column(length = 150, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private StatusType status;  // 글 상태? 판매중? 판매완료? 이런거..?

    private Long viewCount;
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "duckuJangter", cascade = CascadeType.PERSIST)
    private List<JangterImages> jangterImages = new ArrayList<>();

    @Column(name = "similarity", precision = 10, scale = 8)
    private Double similarity;  // TF-IDF 기반 유사도 점수

    // TODO 북마크 연관관계

    public void softDelete() {
        deletedAt = LocalDateTime.now();
    }

    // 이미지 연관관계 메서드
    public void addJangterImage(JangterImages jangterImage) {
        this.jangterImages.add(jangterImage);
        jangterImage.addDuckuJangter(this);
    }
    // 유사도 점수 업데이트 메서드
    public void updateSimilarity(Double similarity) {
        this.similarity = similarity;
    }
}
