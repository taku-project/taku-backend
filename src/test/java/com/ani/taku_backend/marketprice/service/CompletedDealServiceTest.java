package com.ani.taku_backend.marketprice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.marketprice.config.CacheConfig;
import com.ani.taku_backend.marketprice.config.DateConfig;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.MarketPriceSearchResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphRequestDTO;
import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;  // <-- 추가
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
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

    @Autowired
    private CacheConfig cacheConfig;

    /**
     * ★ 캐시 매니저 주입
     */
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ItemCategoriesRepository itemCategoriesRepository;

    @BeforeEach
    void setUp() {
        when(extractKeywordService.extractKeywords(anyString()))
                .thenReturn(List.of("나루토", "피규어"));
        when(extractKeywordService.extractKeywords("존재하지않는상품"))
                .thenReturn(Collections.emptyList());

        createTestProducts();
    }

    /**
     * ★ 각 테스트가 끝난 뒤 캐시를 비움
     */
    @AfterEach
    void clearCaches() {
        // "marketPrice" 라는 캐시가 있다면
        Cache marketPriceCache = cacheManager.getCache("marketPrice");
        if (marketPriceCache != null) {
            marketPriceCache.clear();
        }

        // "weeklyStats" 라는 캐시가 있다면
        Cache weeklyStatsCache = cacheManager.getCache("weeklyStats");
        if (weeklyStatsCache != null) {
            weeklyStatsCache.clear();
        }
    }

    private void createTestProducts() {
        ItemCategories figure = ItemCategories.builder()
                .name("피규어")
                .build();

        itemCategoriesRepository.save(figure);

        DuckuJangter product = DuckuJangter.builder()
                .title("나루토 피규어 신품")
                .price(new BigDecimal("50000"))
                .itemCategories(figure)
                .status(StatusType.ACTIVE)
                .description("테스트용 나루토 피규어입니다.")
                .build();

        duckuJangterRepository.save(product);

        MarketPriceStats stats = MarketPriceStats.builder()
                .product(product)
                .title(product.getTitle())
                .registeredPrice(product.getPrice())
                .soldPrice(new BigDecimal("48000"))
                .registeredDate(LocalDate.now().minusDays(1))
                .build();

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
        PriceGraphRequestDTO requestDTO = new PriceGraphRequestDTO(
                "나루토 피규어",
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                GraphDisplayOption.ALL
        );

        MarketPriceSearchResponseDTO result = completedDealService.searchMarketPrice(
                requestDTO,
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertEquals("나루토 피규어", result.getKeyword());
        assertFalse(result.getPriceGraph().getDataPoints().isEmpty());
        assertNotNull(result.getWeeklyStats());
        assertFalse(result.getSimilarProducts().isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 키워드 검색시 빈 결과 반환")
    void searchMarketPrice_NotFound() {
        PriceGraphRequestDTO requestDTO = new PriceGraphRequestDTO(
                "존재하지않는상품",
                LocalDate.now().minusDays(8),
                LocalDate.now(),
                GraphDisplayOption.ALL
        );

        MarketPriceSearchResponseDTO result = completedDealService.searchMarketPrice(
                requestDTO,
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertTrue(result.getPriceGraph().getDataPoints().isEmpty());
        assertEquals(0, result.getWeeklyStats().getTotalDeals());
        assertTrue(result.getSimilarProducts().isEmpty());
    }

    @Test
    @DisplayName("잘못된 날짜 범위로 검색시 예외 발생")
    void searchMarketPrice_InvalidDateRange() {
        PriceGraphRequestDTO requestDTO = new PriceGraphRequestDTO(
                "나루토 피규어",
                LocalDate.now(),
                LocalDate.now().minusDays(7),
                GraphDisplayOption.ALL
        );

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

        List<CompletedDeal> deals = completedDealService.findRecentDealsInCategory(
                figure,
                LocalDateTime.now().minusDays(30)
        );

        assertFalse(deals.isEmpty());
        deals.forEach(deal ->
                assertEquals("피규어", deal.getCategoryName())
        );
    }
}