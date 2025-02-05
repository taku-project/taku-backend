package com.ani.taku_backend.jangter.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.enums.ProductStatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
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
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ProductStatusType status;  // 상품의 판매 상태 (판매중, 예약중, 거래완료)

    @Column(name = "tfidf_vector",columnDefinition = "TEXT")
    private String tfidfVector;  // TF-IDF 벡터값을 저장.

    private long viewCount;
    private LocalDateTime deletedAt;

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
     * 상품의 판매 상태를 변경합니다.
     * 상태 변경 시 적절한 검증을 수행합니다.
     *
     * @param newStatus 변경할 새로운 상태
     * @throws DuckwhoException 유효하지 않은 상태 변경 시도시 발생
     */
    public void updateStatus(ProductStatusType newStatus) {
        if (newStatus == null) {
            throw new DuckwhoException(ErrorCode.INVALID_PRODUCT_STATUS);
        }
        this.status = newStatus;
    }

    /**
     * 상품을 예약 상태로 변경하고 구매 예정자를 설정합니다.
     * 판매중 상태인 상품만 예약할 수 있습니다.
     *
     * @param buyer 구매 예정자
     * @throws DuckwhoException 이미 예약중이거나 판매 완료된 상품인 경우 발생
     */
    public void reserve(User buyer) {
        if (this.status != ProductStatusType.FOR_SALE) {
            throw new DuckwhoException(ErrorCode.PRODUCT_NOT_FOR_SALE);
        }
        if (buyer == null) {
            throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = ProductStatusType.RESERVED;
        this.buyUser = buyer;
        log.debug("상품 예약 처리 - productId: {}, buyerId: {}", this.id, buyer.getUserId());
    }

    /**
     * 예약된 상품의 거래를 완료 상태로 변경합니다.
     * 예약중 상태인 상품만 거래완료로 변경할 수 있습니다.
     *
     * @throws DuckwhoException 예약중이 아닌 상품을 거래완료로 변경 시도시 발생
     */
    public void completeSale() {
        if (this.status != ProductStatusType.RESERVED) {
            throw new DuckwhoException(ErrorCode.PRODUCT_NOT_RESERVED);
        }
        if (this.buyUser == null) {
            throw new DuckwhoException(ErrorCode.INVALID_PRODUCT_STATUS);
        }
        this.status = ProductStatusType.SOLD_OUT;
        log.debug("상품 판매 완료 처리 - productId: {}, buyerId: {}", this.id, this.buyUser.getUserId());
    }

    /**
     * 예약된 상품의 예약을 취소하고 다시 판매중 상태로 변경합니다.
     * 예약중 상태인 상품만 예약 취소가 가능합니다.
     *
     * @throws DuckwhoException 예약중이 아닌 상품의 예약 취소 시도시 발생
     */
    public void cancelReservation() {
        if (this.status != ProductStatusType.RESERVED) {
            throw new DuckwhoException(ErrorCode.PRODUCT_NOT_RESERVED);
        }
        this.status = ProductStatusType.FOR_SALE;
        this.buyUser = null;
        log.debug("상품 예약 취소 처리 - productId: {}", this.id);
    }

}
