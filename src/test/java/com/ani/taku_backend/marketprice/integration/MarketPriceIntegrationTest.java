//package com.ani.taku_backend.marketprice.integration;
//
//import com.ani.taku_backend.common.enums.StatusType;
//import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
//import com.ani.taku_backend.jangter.model.entity.ItemCategories;
//import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
//import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
//import com.ani.taku_backend.marketprice.config.TestRedisConfig;
//import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
//import com.ani.taku_backend.marketprice.model.dto.*;
//import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
//import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
//import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
//import com.ani.taku_backend.marketprice.repository.MarketPriceStatsRepository;
//import com.ani.taku_backend.marketprice.service.CompletedDealService;
//import com.ani.taku_backend.marketprice.service.MarketPriceStatsService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Import(TestRedisConfig.class)
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class MarketPriceIntegrationTest {
//    @Autowired
//    private CompletedDealService completedDealService;
//
//    @Autowired
//    private MarketPriceStatsService marketPriceStatsService;
//
//    @Autowired
//    private CompletedDealRepository completedDealRepository;
//
//    @Autowired
//    private MarketPriceStatsRepository marketPriceStatsRepository;
//
//    @Autowired
//    private ItemCategoriesRepository itemCategoriesRepository;
//
//    @Autowired
//    private DuckuJangterRepository duckuJangterRepository;
//
//    private final Random random = new Random();
//    private Map<String, ItemCategories> categories;
//    private List<String> figureKeywords;
//    private List<String> modelKitKeywords;
//    private List<String> dollKeywords;
//
//    @BeforeEach
//    void setUp() {
//        categories = createCategories();
//        initializeKeywords();
//        createTestData();
//    }
//
//    private Map<String, ItemCategories> createCategories() {
//        Map<String, ItemCategories> cats = new HashMap<>();
//        cats.put("피규어", itemCategoriesRepository.save(ItemCategories.builder().name("피규어").build()));
//        cats.put("프라모델", itemCategoriesRepository.save(ItemCategories.builder().name("프라모델").build()));
//        cats.put("인형", itemCategoriesRepository.save(ItemCategories.builder().name("인형").build()));
//        return cats;
//    }
//
//    private void initializeKeywords() {
//        figureKeywords = Arrays.asList(
//                "원피스", "나루토", "드래곤볼", "건담", "세일러문",
//                "루피", "조로", "나미", "상디", "우솝"
//        );
//
//        modelKitKeywords = Arrays.asList(
//                "건담", "마징가", "에반게리온", "타이탄", "건프라",
//                "RG", "MG", "PG", "HG", "SD"
//        );
//
//        dollKeywords = Arrays.asList(
//                "바비", "브라이스", "리카", "제니", "실바니안",
//                "디즈니", "산리오", "몬스터하이", "베이비돌", "포셀린"
//        );
//    }
//
//    private void createTestData() {
//        createProductsForCategory("피규어", figureKeywords, new BigDecimal("30000"), new BigDecimal("80000"), 100);
//        createProductsForCategory("프라모델", modelKitKeywords, new BigDecimal("50000"), new BigDecimal("150000"), 100);
//        createProductsForCategory("인형", dollKeywords, new BigDecimal("20000"), new BigDecimal("50000"), 100);
//    }
//    private void createProductsForCategory(String categoryName, List<String> keywords,
//                                           BigDecimal minPrice, BigDecimal maxPrice, int count) {
//        ItemCategories category = categories.get(categoryName);
//
//        for (int i = 0; i < count; i++) {
//            String keyword1 = keywords.get(random.nextInt(keywords.size()));
//            String keyword2 = keywords.get(random.nextInt(keywords.size()));
//            String condition = random.nextBoolean() ? "신품" : "중고";
//            String title = String.format("%s %s #%d %s", keyword1, keyword2, i + 1, condition);
//
//            BigDecimal price = minPrice.add(
//                    new BigDecimal(random.nextDouble()).multiply(maxPrice.subtract(minPrice))
//            );
//
//            DuckuJangter product = createAndSaveProduct(title, price, category);
//            MarketPriceStats stats = createAndSaveStats(product);
//
//            CompletedDeal deal = createAndSaveCompletedDeal(product, stats);
//            if (deal != null) {
//                System.out.println("Created deal: " + deal.getTitle());
//            }
//        }
//    }
//
//    private DuckuJangter createAndSaveProduct(String title, BigDecimal price, ItemCategories category) {
//        DuckuJangter product = DuckuJangter.builder()
//                .title(title)
//                .price(price)
//                .status(StatusType.ACTIVE)
//                .viewCount((long) random.nextInt(1000))
//                .itemCategories(category)
//                .description(generateDescription(title))
//                .build();
//        return duckuJangterRepository.save(product);
//    }
//
//    private String generateDescription(String title) {
//        return String.format("%s 상품입니다. 상태 양호, 즉시 거래 가능합니다.", title);
//    }
//
//    private MarketPriceStats createAndSaveStats(DuckuJangter product) {
//        BigDecimal soldPrice = null;
//        if (random.nextDouble() < 0.8) {
//            double ratio = 0.8 + (random.nextDouble() * 0.4);
//            soldPrice = product.getPrice().multiply(new BigDecimal(ratio));
//        }
//
//        MarketPriceStats stats = MarketPriceStats.builder()
//                .product(product)
//                .title(product.getTitle())
//                .registeredPrice(product.getPrice())
//                .soldPrice(soldPrice)
//                .registeredDate(LocalDate.now().minusDays(random.nextInt(30)))
//                .build();
//        return marketPriceStatsRepository.save(stats);
//    }
//
//    private CompletedDeal createAndSaveCompletedDeal(DuckuJangter product, MarketPriceStats stats) {
//        if (stats.getSoldPrice() == null) return null;
//
//        CompletedDeal deal = CompletedDeal.builder()
//                .product(product)
//                .marketPriceStats(stats)
//                .title(product.getTitle())
//                .price(stats.getSoldPrice())
//                .categoryName(product.getItemCategories().getName())
//                .searchKeywords(generateSearchKeywords(product.getTitle()))
//                .build();
//        return completedDealRepository.save(deal);
//    }
//
//    private String generateSearchKeywords(String title) {
//        return title.toLowerCase()
//                .replaceAll("[^a-z0-9가-힣\\s]", " ")
//                .replaceAll("\\s+", " ")
//                .trim();
//    }
//
//    @Test
//    @DisplayName("테스트 데이터 저장 확인")
//    void checkTestDataSaved() {
//        List<CompletedDeal> deals = completedDealRepository.findAll();
//        assertFalse(deals.isEmpty());
//        System.out.println("저장된 거래 수: " + deals.size());
//
//        List<MarketPriceStats> stats = marketPriceStatsRepository.findAll();
//        assertFalse(stats.isEmpty());
//        System.out.println("저장된 통계 수: " + stats.size());
//    }
//
//
//
//    @Test
//    @DisplayName("피규어 카테고리 시세 조회 통합 테스트")
//    void figurePriceTest() {
//        String keyword = "원피스";
//        LocalDate endDate = LocalDate.now();
//        LocalDate startDate = endDate.minusDays(30);
//
//        PriceGraphRequestDTO request = new PriceGraphRequestDTO(
//                keyword, startDate, endDate, GraphDisplayOption.ALL
//        );
//        MarketPriceSearchResponseDTO result =
//                completedDealService.searchMarketPrice(request, PageRequest.of(0, 10));
//
//        assertNotNull(result);
//        assertNotNull(result.getPriceGraph());
//        assertFalse(result.getPriceGraph().getDataPoints().isEmpty());
//
//        result.getPriceGraph().getDataPoints().forEach(point -> {
//            if (point.getRegisteredPrice() != null) {
//                assertTrue(point.getRegisteredPrice().compareTo(new BigDecimal("30000")) >= 0);
//                assertTrue(point.getRegisteredPrice().compareTo(new BigDecimal("80000")) <= 0);
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("프라모델 카테고리 시세 조회 통합 테스트")
//    void modelKitPriceTest() {
//        String keyword = "건담 프라모델";
//
//        WeeklyStatsResponseDTO stats = marketPriceStatsService.getWeeklyStats(keyword);
//
//        assertNotNull(stats);
//        assertTrue(stats.getTotalDeals() > 0);
//        assertTrue(stats.getLowestPrice().compareTo(new BigDecimal("50000")) >= 0);
//        assertTrue(stats.getHighestPrice().compareTo(new BigDecimal("150000")) <= 0);
//    }
//
//    @Test
//    @DisplayName("카테고리별 CompletedDeal 생성 확인")
//    void checkCompletedDealCreation() {
//        // 각 카테고리별로 CompletedDeal 확인
//        Map<String, Integer> categoryDeals = new HashMap<>();
//
//        List<CompletedDeal> allDeals = completedDealRepository.findAll();
//        for (CompletedDeal deal : allDeals) {
//            categoryDeals.merge(deal.getCategoryName(), 1, Integer::sum);
//        }
//
//        // 각 카테고리별 생성된 CompletedDeal 수 출력
//        categoryDeals.forEach((category, count) ->
//                System.out.println(category + " 카테고리의 CompletedDeal 수: " + count));
//
//        // 모든 카테고리에 대해 CompletedDeal이 존재하는지 확인
//        assertTrue(categoryDeals.containsKey("피규어"));
//        assertTrue(categoryDeals.containsKey("프라모델"));
//        assertTrue(categoryDeals.containsKey("인형"));
//
//        // 각 카테고리별로 충분한 수의 CompletedDeal이 생성되었는지 확인
//        categoryDeals.values().forEach(count ->
//                assertTrue(count > 0, "각 카테고리는 최소 1개 이상의 CompletedDeal을 가져야 합니다"));
//    }
//
//    @Test
//    @DisplayName("카테고리별 거래 완료 데이터 검증")
//    void categoryCompletedDealsTest() {
//        String categoryName = "피규어";
//        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
//
//        List<CompletedDeal> deals = completedDealRepository
//                .findByCategoryNameAndCreatedAtAfterOrderByCreatedAtDesc(
//                        categoryName, thirtyDaysAgo
//                );
//
//        assertFalse(deals.isEmpty());
//        deals.forEach(deal ->
//                assertEquals(categoryName, deal.getCategoryName())
//        );
//    }
//
//    @Test
//    @DisplayName("가격 변동 추이 검증")
//    void priceVariationTest() {
//        LocalDate startDate = LocalDate.now().minusDays(30);
//        LocalDate endDate = LocalDate.now();
//
//        List<MarketPriceStats> stats = marketPriceStatsRepository
//                .findByRegisteredDateBetween(startDate, endDate);
//
//        assertFalse(stats.isEmpty());
//        stats.forEach(stat -> {
//            if (stat.getSoldPrice() != null) {
//                BigDecimal minExpected = stat.getRegisteredPrice().multiply(new BigDecimal("0.8"));
//                BigDecimal maxExpected = stat.getRegisteredPrice().multiply(new BigDecimal("1.2"));
//                assertTrue(stat.getSoldPrice().compareTo(minExpected) >= 0);
//                assertTrue(stat.getSoldPrice().compareTo(maxExpected) <= 0);
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("검색 키워드 매칭 테스트")
//    void searchKeywordMatchingTest() {
//        // Given
//        String keyword = "원피스";
//        PriceGraphRequestDTO request = new PriceGraphRequestDTO(
//                keyword,
//                LocalDate.now().minusDays(30),
//                LocalDate.now(),
//                GraphDisplayOption.ALL
//        );
//
//        int expectedMinResults = 1;
//
//        // When
//        MarketPriceSearchResponseDTO result =
//                completedDealService.searchMarketPrice(request, PageRequest.of(0, 50));
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.getSimilarProducts().size() >= expectedMinResults);
//        assertTrue(result.getPriceGraph().getDataPoints().size() > 0);
//        result.getSimilarProducts().forEach(product ->
//                assertTrue(product.getTitle().toLowerCase().contains(keyword.toLowerCase()))
//        );
//    }
//
//    @Test
//    @DisplayName("카테고리별 가격 분포 검증")
//    void categoryPriceDistributionTest() {
//        Map<String, BigDecimal[]> expectedRanges = Map.of(
//                "피규어", new BigDecimal[]{new BigDecimal("30000"), new BigDecimal("80000")},
//                "프라모델", new BigDecimal[]{new BigDecimal("50000"), new BigDecimal("150000")},
//                "인형", new BigDecimal[]{new BigDecimal("20000"), new BigDecimal("50000")}
//        );
//
//        expectedRanges.forEach((category, range) -> {
//            List<MarketPriceStats> stats = marketPriceStatsRepository.findAll().stream()
//                    .filter(stat -> stat.getTitle().contains(category))
//                    .toList();
//
//            assertFalse(stats.isEmpty());
//            stats.forEach(stat -> {
//                assertTrue(stat.getRegisteredPrice().compareTo(range[0]) >= 0);
//                assertTrue(stat.getRegisteredPrice().compareTo(range[1]) <= 0);
//            });
//        });
//    }
//
//    @Test
//    @DisplayName("키워드 검색 결과 수 검증")
//    void keywordSearchCountTest() {
//        String[] keywords = {"원피스", "건담", "바비"};
//        int expectedMinResults = 10;
//
//        for (String keyword : keywords) {
//            PriceGraphRequestDTO request = new PriceGraphRequestDTO(
//                    keyword,
//                    LocalDate.now().minusDays(30),
//                    LocalDate.now(),
//                    GraphDisplayOption.ALL
//            );
//
//            MarketPriceSearchResponseDTO result =
//                    completedDealService.searchMarketPrice(request, PageRequest.of(0, 50));
//
//            assertNotNull(result);
//            assertTrue(result.getSimilarProducts().size() >= expectedMinResults);
//        }
//    }
//
///*    @Test
//    @DisplayName("거래 완료율 검증")
//    void dealCompletionRateTest() {
//        LocalDate startDate = LocalDate.now().minusDays(30);
//        LocalDate endDate = LocalDate.now();
//
//        List<MarketPriceStats> allStats = marketPriceStatsRepository
//                .findByRegisteredDateBetween(startDate, endDate);
//
//        long completedDeals = allStats.stream()
//                .filter(stat -> stat.getSoldPrice() != null)
//                .count();
//
//        double completionRate = (double) completedDeals / allStats.size();
//        assertTrue(completionRate >= 0.7);
//        assertTrue(completionRate <= 0.9);
//    }*/
//}