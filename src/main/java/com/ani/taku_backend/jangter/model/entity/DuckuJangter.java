package com.ani.taku_backend.jangter.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
    private ProductStatus status;  // 상품 상태 (FOR_SALE, RESERVED, SOLD_OUT)

    @Column(name = "tfidf_vector",columnDefinition = "TEXT")
    private String tfidfVector;  // TF-IDF 벡터값을 저장.

    private long viewCount;
    private LocalDateTime deletedAt;

//    @Column(name = "buy_user_id")
//    private Long buyUserId;

    @Builder.Default
    @OneToMany(mappedBy = "duckuJangter", cascade = CascadeType.PERSIST)
    private List<JangterImages> jangterImages = new ArrayList<>();

    // TODO 북마크 연관관계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_user_id")
    private User buyUser;

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

    public long addViewCount(boolean isFirstView) {
        if (isFirstView) {
            return viewCount += 1;
        }
        return viewCount;
    }
    public void updateTfidfVector(String tfidfVector) {
        this.tfidfVector = tfidfVector;
    }

    public static DuckuJangter reference(Long id) {
        DuckuJangter duckuJangter = new DuckuJangter();
        duckuJangter.id = id;
        return duckuJangter;
    }

    /**
     * 상품 상태 변경
     */
    public void updateStatus(ProductStatus newStatus, BigDecimal soldPrice) {
        // 상태 전환 가능 여부 검증
        this.status.validateTransitionTo(newStatus);
        
        // 상태 변경
        this.status = newStatus;
        
        // SOLD_OUT인 경우 구매자 정보와 판매가 업데이트
        if (newStatus == ProductStatus.SOLD_OUT) {
            this.price = soldPrice;
        }
    }

    public boolean isOwner(Long userId) {
        return this.user != null && this.user.getUserId().equals(userId);
    }
    
    /**
     * 채팅방 생성을 위한 상품 상태 검증
     * 상품이 판매 중 상태가 아니면 예외를 발생시킵니다.
     * 
     * @throws DuckwhoException 상품이 판매 중이 아닌 경우
     */
    public void validateForChatRoom() {
        if (this.status != ProductStatus.FOR_SALE) {
            throw new DuckwhoException(ErrorCode.INVALID_PRODUCT_STATUS);
        }
    }
    
    /**
     * 판매자와 구매자가 동일인물인지 검증합니다.
     * 
     * @param buyerId 구매자 ID
     * @throws DuckwhoException 판매자와 구매자가 동일인물인 경우
     */
    public void validateDifferentUsers(Long buyerId) {
        if (this.user != null && this.user.getUserId().equals(buyerId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
    }

}
