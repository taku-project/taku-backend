package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductStatusUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
import com.ani.taku_backend.marketprice.repository.MarketPriceStatsRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DuckuJangterServiceTest {

    @Autowired
    private DuckuJangterService duckuJangterService;

    @Autowired
    private DuckuJangterRepository duckuJangterRepository;

    @Autowired
    private CompletedDealRepository completedDealRepository;

    @Autowired
    private MarketPriceStatsRepository marketPriceStatsRepository;

    @Autowired
    private ItemCategoriesRepository itemCategoriesRepository;

    @Autowired
    private UserRepository userRepository;

    private User seller;
    private User buyer;
    private ItemCategories category;
    private DuckuJangter product;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .email("seller@test.com")
                .nickname("판매자")
                .role(UserRole.USER)
                .build();
        seller = userRepository.save(seller);

        buyer = User.builder()
                .email("buyer@test.com")
                .nickname("구매자")
                .role(UserRole.USER)
                .build();
        buyer = userRepository.save(buyer);

        category = ItemCategories.builder()
                .name("테스트 카테고리")
                .build();
        category = itemCategoriesRepository.save(category);

        product = DuckuJangter.builder()
                .user(seller)
                .itemCategories(category)
                .title("테스트 상품")
                .description("테스트 상품 설명")
                .price(BigDecimal.valueOf(50000))
                .status(ProductStatus.FOR_SALE)
                .build();

        product = duckuJangterRepository.save(product);

        MarketPriceStats stats = MarketPriceStats.builder()
                .product(product)
                .title(product.getTitle())
                .registeredPrice(product.getPrice())
                .registeredDate(LocalDate.now())
                .build();
        marketPriceStatsRepository.save(stats);
    }

    @Test
    @DisplayName("정상적인 판매 흐름 테스트: FOR_SALE -> RESERVED -> SOLD_OUT")
    void testNormalSaleFlow() {
        // Given
        Long productId = product.getId();
        
        // When: FOR_SALE -> RESERVED
        ProductStatusUpdateRequestDTO reserveRequest = new ProductStatusUpdateRequestDTO(ProductStatus.RESERVED, null);
        duckuJangterService.updateProductStatus(productId, reserveRequest, seller);

        // Then: 상태가 RESERVED로 변경되었는지 확인
        DuckuJangter reserved = duckuJangterRepository.findById(productId).orElseThrow();
        assertThat(reserved.getStatus()).isEqualTo(ProductStatus.RESERVED);

        // When: RESERVED -> SOLD_OUT
        ProductStatusUpdateRequestDTO soldOutRequest = new ProductStatusUpdateRequestDTO(ProductStatus.SOLD_OUT, 45000L);
        duckuJangterService.updateProductStatus(productId, soldOutRequest, seller);

        // Then: 상태가 SOLD_OUT으로 변경되었는지 확인
        DuckuJangter soldOut = duckuJangterRepository.findById(productId).orElseThrow();
        assertThat(soldOut.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

        // CompletedDeal이 생성되었는지 확인
        Optional<CompletedDeal> completedDeal = completedDealRepository.findByProductId(productId);
        assertThat(completedDeal).isPresent();
        assertThat(completedDeal.get().getPrice()).isEqualTo(BigDecimal.valueOf(45000));

        // MarketPriceStats가 업데이트되었는지 확인
        Optional<MarketPriceStats> stats = marketPriceStatsRepository.findLatestByProductId(productId);
        assertThat(stats).isPresent();
        assertThat(stats.get().getSoldPrice()).isEqualTo(BigDecimal.valueOf(45000));
    }

    @Test
    @DisplayName("FOR_SALE -> SOLD_OUT 직접 전환 테스트")
    void testDirectSoldOut() {
        // Given
        Long productId = product.getId();

        // When: FOR_SALE -> SOLD_OUT
        ProductStatusUpdateRequestDTO soldOutRequest = new ProductStatusUpdateRequestDTO(ProductStatus.SOLD_OUT, 45000L);
        duckuJangterService.updateProductStatus(productId, soldOutRequest, seller);

        // Then
        DuckuJangter soldOut = duckuJangterRepository.findById(productId).orElseThrow();
        assertThat(soldOut.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

        // CompletedDeal 확인
        Optional<CompletedDeal> completedDeal = completedDealRepository.findByProductId(productId);
        assertThat(completedDeal).isPresent();
    }

    @Test
    @DisplayName("SOLD_OUT 상태에서 다른 상태로 변경 시도 시 실패")
    void testCannotChangeFromSoldOut() {
        // Given: 상품을 SOLD_OUT 상태로 만듦
        Long productId = product.getId();
        ProductStatusUpdateRequestDTO soldOutRequest = new ProductStatusUpdateRequestDTO(ProductStatus.SOLD_OUT, 45000L);
        duckuJangterService.updateProductStatus(productId, soldOutRequest, seller);

        // When & Then: SOLD_OUT -> FOR_SALE 시도 시 예외 발생
        ProductStatusUpdateRequestDTO forSaleRequest = new ProductStatusUpdateRequestDTO(ProductStatus.FOR_SALE, null);
        
        assertThatThrownBy(() -> 
            duckuJangterService.updateProductStatus(productId, forSaleRequest, seller)
        ).isInstanceOf(DuckwhoException.class);
    }

    @Test
    @DisplayName("판매 완료 시 soldPrice 없으면 실패")
    void testSoldOutRequiresSoldPrice() {
        // Given
        Long productId = product.getId();

        // When & Then: soldPrice 없이 SOLD_OUT 시도 시 예외 발생
        ProductStatusUpdateRequestDTO request = new ProductStatusUpdateRequestDTO(ProductStatus.SOLD_OUT, null);
        
        assertThatThrownBy(() -> 
            duckuJangterService.updateProductStatus(productId, request, seller)
        )
        .isInstanceOf(DuckwhoException.class)
        .hasMessage("판매 완료 시 판매가는 필수입니다.");
    }

    @Test
    @DisplayName("상품 소유자가 아닌 사용자가 상태 변경 시도 시 실패")
    void testUnauthorizedStatusUpdate() {
        // Given
        Long productId = product.getId();
        ProductStatusUpdateRequestDTO request = new ProductStatusUpdateRequestDTO(ProductStatus.RESERVED, null);

        // When & Then: 구매자가 상태 변경 시도 시 예외 발생
        assertThatThrownBy(() -> 
            duckuJangterService.updateProductStatus(productId, request, buyer)
        ).isInstanceOf(DuckwhoException.class);
    }

    @Test
    @DisplayName("RESERVED -> FOR_SALE 상태 복귀 테스트")
    void testRevertToForSale() {
        // Given: 상품을 RESERVED 상태로 만듦
        Long productId = product.getId();
        ProductStatusUpdateRequestDTO reserveRequest = new ProductStatusUpdateRequestDTO(ProductStatus.RESERVED, null);
        duckuJangterService.updateProductStatus(productId, reserveRequest, seller);

        // When: RESERVED -> FOR_SALE
        ProductStatusUpdateRequestDTO forSaleRequest = new ProductStatusUpdateRequestDTO(ProductStatus.FOR_SALE, null);
        duckuJangterService.updateProductStatus(productId, forSaleRequest, seller);

        // Then
        DuckuJangter forSale = duckuJangterRepository.findById(productId).orElseThrow();
        assertThat(forSale.getStatus()).isEqualTo(ProductStatus.FOR_SALE);
    }

    @Test
    @DisplayName("동일한 상태로 변경 시도 시 실패")
    void testSameStatusTransition() {
        // Given
        Long productId = product.getId();
        
        // When & Then: FOR_SALE -> FOR_SALE 시도 시 예외 발생
        ProductStatusUpdateRequestDTO request = new ProductStatusUpdateRequestDTO(ProductStatus.FOR_SALE, null);
        
        assertThatThrownBy(() -> 
            duckuJangterService.updateProductStatus(productId, request, seller)
        ).isInstanceOf(DuckwhoException.class);
    }

    @Test
    @DisplayName("판매 완료 시 가격이 음수인 경우 실패")
    void testNegativeSoldPrice() {
        // Given
        Long productId = product.getId();

        // When & Then: 음수 가격으로 SOLD_OUT 시도 시 예외 발생
        ProductStatusUpdateRequestDTO request = new ProductStatusUpdateRequestDTO(ProductStatus.SOLD_OUT, -1000L);
        
        assertThatThrownBy(() -> 
            duckuJangterService.updateProductStatus(productId, request, seller)
        )
        .isInstanceOf(DuckwhoException.class)
        .hasMessage("잘못된 입력값입니다.");
    }

    @Test
    @DisplayName("삭제된 상품의 상태 변경 시도 시 실패")
    void testUpdateDeletedProduct() {
        // Given: 상품을 삭제 상태로 만듦
        Long productId = product.getId();
        product.delete();
        duckuJangterRepository.save(product);

        // When & Then: 삭제된 상품 상태 변경 시도 시 예외 발생
        ProductStatusUpdateRequestDTO request = new ProductStatusUpdateRequestDTO(ProductStatus.RESERVED, null);
        
        assertThatThrownBy(() -> 
            duckuJangterService.updateProductStatus(productId, request, seller)
        ).isInstanceOf(DuckwhoException.class);
    }

    @Test
    @DisplayName("SOLD_OUT 시 MarketPriceStats 업데이트 검증")
    void testMarketPriceStatsUpdate() {
        // Given
        Long productId = product.getId();
        BigDecimal originalPrice = BigDecimal.valueOf(50000);
        BigDecimal soldPrice = BigDecimal.valueOf(45000);

        // When: 상품을 SOLD_OUT으로 변경
        ProductStatusUpdateRequestDTO request = new ProductStatusUpdateRequestDTO(ProductStatus.SOLD_OUT, soldPrice.longValue());
        duckuJangterService.updateProductStatus(productId, request, seller);

        // Then: MarketPriceStats 검증
        Optional<MarketPriceStats> stats = marketPriceStatsRepository.findLatestByProductId(productId);
        assertThat(stats).isPresent();
        MarketPriceStats marketPriceStats = stats.get();
        
        // 원래 가격과 판매 가격이 정확히 기록되었는지 확인
        assertThat(marketPriceStats.getRegisteredPrice()).isEqualTo(originalPrice);
        assertThat(marketPriceStats.getSoldPrice()).isEqualTo(soldPrice);
        
        // 가격 변동률이 정확한지 확인 (-10%)
        BigDecimal actualPriceChange = soldPrice.subtract(originalPrice)
            .divide(originalPrice, 2, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
            
        // 소수점 2자리까지 비교
        assertThat(actualPriceChange.setScale(2, java.math.RoundingMode.HALF_UP))
            .isEqualTo(BigDecimal.valueOf(-10.00).setScale(2, java.math.RoundingMode.HALF_UP));
    }
} 
