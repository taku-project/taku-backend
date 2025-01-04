package com.ani.taku_backend.jangter.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
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
    private ItemCategories itemCategories;

    @Column(length = 150, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private StatusType status;  // 글 상태? 판매중? 판매완료? 이런거..?

    private long viewCount;
    private LocalDateTime deletedAt;

    @Column(name = "buy_user_id")
    private Long buyUserId;

    @Builder.Default
    @OneToMany(mappedBy = "duckuJangter", cascade = CascadeType.PERSIST)
    private List<JangterImages> jangterImages = new ArrayList<>();

    // TODO 북마크 연관관계

    public void delete() {
        deletedAt = LocalDateTime.now();
    }

    // 이미지 연관관계 메서드
    public void addJangterImage(JangterImages jangterImage) {
        this.jangterImages.add(jangterImage);
        jangterImage.addDuckuJangter(this);
    }

    /**
     * 업데이트 메서드, 기존과 변경이 없다면 업데이트 하지 않음
     */
    public void updateProduct(ProductUpdateRequestDTO productUpdateRequestDTO, ItemCategories itemCategories) {
        String updateTitle = productUpdateRequestDTO.getTitle();
        String updateDescription = productUpdateRequestDTO.getDescription();
        BigDecimal updatePrice = productUpdateRequestDTO.getPrice();

        if (updateTitle != null && !updateTitle.equals(this.title)) {
            this.title = updateTitle;
        }
        if (updateDescription != null && !updateDescription.equals(this.description)) {
            this.description = updateDescription;
        }
        if (updatePrice != null && !updatePrice.equals(this.price)) {
            this.price = updatePrice;
        }
        if (itemCategories != null && !itemCategories.equals(this.itemCategories)) {
            this.itemCategories = itemCategories;
        }
    }


    public long addViewCount() {
        return viewCount += 1;
    }
}
