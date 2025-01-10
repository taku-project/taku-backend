package com.ani.taku_backend.marketprice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.marketprice.config.DateConfig;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphRequestDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
@Transactional
class CompletedDealServiceTest {

    @Autowired
    private CompletedDealService completedDealService;

    @Autowired
    private CompletedDealRepository completedDealRepository;

    @Autowired
    private DuckuJangterRepository duckuJangterRepository;

    @MockBean
    private ExtractKeywordService extractKeywordService;

    @Autowired
    private DateConfig dateConfig;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        when(extractKeywordService.extractKeywords(anyString()))
                .thenReturn(List.of("나루토", "피규어"));

        // 테스트용 상품 데이터 생성
        createTestProducts();
    }

    private void createTestProducts() {
        ItemCategories figure = ItemCategories.builder()
                .name("피규어")
                .build();

        DuckuJangter product = DuckuJangter.builder()
                .title("나루토 피규어 신품")
                .price(new BigDecimal("50000"))
                .itemCategories(figure)
                .status(StatusType.ACTIVE)
                .build();

        duckuJangterRepository.save(product);

        // 시세 데이터 생성
        MarketPriceStats stats = MarketPriceStats.builder()
                .product(product)
                .title(product.getTitle())
                .registeredPrice(product.getPrice())
                .soldPrice(new BigDecimal("48000"))
                .registeredDate(LocalDate.now().minusDays(1))
                .build();

        // 거래 완료 데이터 생성
        CompletedDeal deal = CompletedDeal.builder()
                .product(product)
                .marketPriceStats(stats)
                .title(product.getTitle())
                .price(new BigDecimal("48000"))
                .categoryName("피규어")
                .searchKeywords("나루토 피규어")
                .build();

        completedDealRepository.save(deal);
    }

    @Test
    @DisplayName("정상적인 시세 검색")
    void searchMarketPrice_Success() {
        // given
        PriceGraphRequestDTO requestDTO = new PriceGraphRequestDTO(
                "나루토 피규어",
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                GraphDisplayOption.ALL
        );

        // when
        MarketPriceSearchResponseDTO result = completedDealService.searchMarketPrice(
                requestDTO,
                PageRequest.of(0, 10)
        );

        // then
        assertNotNull(result);
        assertEquals("나루토 피규어", result.getKeyword());
        assertFalse(result.getPriceGraph().getDataPoints().isEmpty());
        assertNotNull(result.getWeeklyStats());
        assertFalse(result.getSimilarProducts().isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 키워드 검색시 빈 결과 반환")
    void searchMarketPrice_NotFound() {
        // given
        PriceGraphRequestDTO requestDTO = new PriceGraphRequestDTO(
                "존재하지않는상품",
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                GraphDisplayOption.ALL
        );

        // when
        MarketPriceSearchResponseDTO result = completedDealService.searchMarketPrice(
                requestDTO,
                PageRequest.of(0, 10)
        );

        // then
        assertNotNull(result);
        assertTrue(result.getPriceGraph().getDataPoints().isEmpty());
        assertEquals(0, result.getWeeklyStats().getTotalDeals());
        assertTrue(result.getSimilarProducts().isEmpty());
    }

    @Test
    @DisplayName("잘못된 날짜 범위로 검색시 예외 발생")
    void searchMarketPrice_InvalidDateRange() {
        // given
        PriceGraphRequestDTO requestDTO = new PriceGraphRequestDTO(
                "나루토 피규어",
                LocalDate.now(),
                LocalDate.now().minusDays(7),  // 시작일이 종료일보다 늦음
                GraphDisplayOption.ALL
        );

        // when & then
        assertThrows(DuckwhoException.class, () ->
                completedDealService.searchMarketPrice(requestDTO, PageRequest.of(0, 10))
        );
    }

    @Test
    @DisplayName("카테고리별 최근 거래 조회")
    void findRecentDealsInCategory_Success() {

        ItemCategories figure = ItemCategories.builder()
                .name("피규어")
                .build();

        // when
        List<CompletedDeal> deals = completedDealService.findRecentDealsInCategory(
                figure,
                LocalDateTime.now().minusDays(30)
        );

        // then
        assertFalse(deals.isEmpty());
        deals.forEach(deal ->
                assertEquals("피규어", deal.getCategoryName())
        );
    }
}