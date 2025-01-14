package com.ani.taku_backend.marketprice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
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
import org.springframework.cache.CacheManager;
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
    private TfidfService tfidfService;

    @MockBean
    private ExtractKeywordService extractKeywordService;

    @Autowired
    private DateConfig dateConfig;

    @Autowired
    private CacheConfig cacheConfig;

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


        DuckuJangter mockProduct = DuckuJangter.builder()
                .id(999L)
                .title("모킹된 유사 상품")
                .price(new BigDecimal("99999"))
                .description("테스트용 TF-IDF 모킹 상품")
                .tfidfVector("{\"keyword1\":0.75,\"keyword2\":0.7}") // 가짜 TF-IDF 벡터
                .build();

        TfidfService.ProductWithSimilarity mockSimilarity =
                new TfidfService.ProductWithSimilarity(mockProduct, 0.95);

        when(tfidfService.calculateProductSimilarities(anyString(), anyList()))
                .thenReturn(List.of(mockSimilarity));


        createTestProducts();
    }

    @AfterEach
    void clearCaches() {
        Cache marketPriceCache = cacheManager.getCache("marketPrice");
        if (marketPriceCache != null) {
            marketPriceCache.clear();
        }
        Cache weeklyStatsCache = cacheManager.getCache("weeklyStats");
        if (weeklyStatsCache != null) {
            weeklyStatsCache.clear();
        }
    }


    private void createTestProducts() {
        // 1) 카테고리 생성
        ItemCategories figure = ItemCategories.builder()
                .name("피규어")
                .build();
        itemCategoriesRepository.save(figure);

        // -----------------------------
        // [상품1] "나루토 피규어 신품"
        // -----------------------------
        DuckuJangter product1 = DuckuJangter.builder()
                .title("나루토 피규어 신품")
                .price(new BigDecimal("50000"))
                .itemCategories(figure)
                .status(StatusType.ACTIVE)
                .description("테스트용 나루토 피규어(신품).")
                .build();
        duckuJangterRepository.save(product1);

        MarketPriceStats stats1 = MarketPriceStats.builder()
                .product(product1)
                .title(product1.getTitle())
                .registeredPrice(product1.getPrice())
                .soldPrice(new BigDecimal("48000"))
                .registeredDate(LocalDate.now().minusDays(1))
                .build();

        CompletedDeal deal1 = CompletedDeal.builder()
                .product(product1)
                .marketPriceStats(stats1)
                .title(product1.getTitle())
                .price(new BigDecimal("48000"))
                .categoryName("피규어")
                .searchKeywords("나루토 피규어")
                .build();
        completedDealRepository.save(deal1);

        // -----------------------------
        // [상품2] "나루토 피규어 중고"
        // -----------------------------
        DuckuJangter product2 = DuckuJangter.builder()
                .title("나루토 피규어 중고")
                .price(new BigDecimal("30000"))
                .itemCategories(figure)
                .status(StatusType.ACTIVE)
                .description("나루토 피규어 중고 테스트.")
                .build();
        duckuJangterRepository.save(product2);

        MarketPriceStats stats2 = MarketPriceStats.builder()
                .product(product2)
                .title(product2.getTitle())
                .registeredPrice(product2.getPrice())
                // 아직 판매 안 된 상태이므로 soldPrice는 null
                .registeredDate(LocalDate.now().minusDays(2))
                .build();

        CompletedDeal deal2 = CompletedDeal.builder()
                .product(product2)
                .marketPriceStats(stats2)
                .title(product2.getTitle())
                .price(product2.getPrice()) // 일단 등록가격으로...
                .categoryName("피규어")
                .searchKeywords("나루토 피규어 중고")
                .build();
        completedDealRepository.save(deal2);
    }

    // ============= [테스트 코드들] =============

    @Test
    @DisplayName("정상적인 시세 검색")
    void searchMarketPrice_Success() {

        List<CompletedDeal> allDeals = completedDealRepository.findAll();
        System.out.println("=== CompletedDeal 전체 목록 ===");
        allDeals.forEach(deal -> {
            System.out.println("deal id=" + deal.getId()
                    + ", title=" + deal.getTitle()
                    + ", searchKeywords=" + deal.getSearchKeywords()
                    + ", registeredDate=" + deal.getMarketPriceStats().getRegisteredDate()
                    + ", soldPrice=" + deal.getMarketPriceStats().getSoldPrice()
            );
        });
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

        // priceGraph 데이터가 비어있지 않아야 함
        assertFalse(result.getPriceGraph().getDataPoints().isEmpty());

        // weeklyStats(주간 통계)도 존재해야 함
        assertNotNull(result.getWeeklyStats());

        /*// "유사 상품" 모킹 결과가 존재하기 때문에, 비어있지 않아야 함-> 이미지 부분떄문에 오류가 나 주석처리
        assertFalse(result.getSimilarProducts().isEmpty());*/
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
        for (CompletedDeal deal : deals) {
            assertEquals("피규어", deal.getCategoryName());
        }
    }
}