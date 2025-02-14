//package com.ani.taku_backend.marketprice.repository;
//
//import com.ani.taku_backend.config.QueryDslConfig;
//import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
//import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
//import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
//import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(
//        classes = {
//                MarketPriceStatsQueryRepositoryImplTest.TestConfig.class,
//                QueryDslConfig.class,
//                MarketPriceStatsQueryRepositoryImpl.class
//        },
//        properties = {
//                // MongoDB 리포지토리 스캔 및 자동구성 제외
//                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
//                "spring.data.mongodb.repositories.enabled=false"
//        }
//)
//@ActiveProfiles("test")
//@Transactional
//@TestPropertySource(properties = {
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.jpa.show-sql=true",
//        "spring.jpa.properties.hibernate.format_sql=true",
//        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
//        "spring.datasource.username=sa",
//        "spring.datasource.password="
//})
//class MarketPriceStatsQueryRepositoryImplTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private JPAQueryFactory queryFactory;
//
//    @Autowired
//    private MarketPriceStatsQueryRepositoryImpl repository;
//
//    @BeforeEach
//    void setUp() {
//        setupTestData();
//    }
//
//    private void setupTestData() {
//        MarketPriceStats stats1 = MarketPriceStats.builder()
//                .title("원피스 피규어")
//                .registeredPrice(new BigDecimal("50000"))
//                .soldPrice(new BigDecimal("45000"))
//                .registeredDate(LocalDate.now().minusDays(5))
//                .build();
//
//        MarketPriceStats stats2 = MarketPriceStats.builder()
//                .title("원피스 피규어")
//                .registeredPrice(new BigDecimal("55000"))
//                .soldPrice(new BigDecimal("48000"))
//                .registeredDate(LocalDate.now().minusDays(3))
//                .build();
//
//        MarketPriceStats stats3 = MarketPriceStats.builder()
//                .title("원피스 피규어")
//                .registeredPrice(new BigDecimal("45000"))
//                .soldPrice(new BigDecimal("40000"))
//                .registeredDate(LocalDate.now().minusDays(1))
//                .build();
//
//        entityManager.persist(stats1);
//        entityManager.persist(stats2);
//        entityManager.persist(stats3);
//        entityManager.flush();
//    }
//
//    @Test
//    @DisplayName("getPriceGraph 메서드는 평균 판매가를 정확히 계산해야 한다")
//    void getPriceGraphShouldCalculateAverageSoldPrice() {
//        String title = "원피스 피규어";
//        LocalDate fromDate = LocalDate.now().minusDays(7);
//        LocalDate toDate = LocalDate.now();
//        GraphDisplayOption option = GraphDisplayOption.ALL;
//
//        PriceGraphResponseDTO result = repository.getPriceGraph(title, fromDate, toDate, option);
//
//        assertThat(result).isNotNull();
//        assertThat(result.periodAvgSoldPrice()).isNotNull();
//        // (45000 + 48000 + 40000) / 3 ≈ 44333.33
//        assertThat(result.periodAvgSoldPrice())
//                .isEqualByComparingTo(new BigDecimal("44333.33"));
//        assertThat(result.dataPoints()).hasSize(3);
//        assertThat(result.dataPoints().get(0).soldPrice())
//                .isEqualByComparingTo(new BigDecimal("45000"));
//    }
//
//    @Configuration
//    // 이 설정 클래스에서는 MongoDB 관련 자동구성을 제외함
//    @EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
//    static class TestConfig {
//        // 필요시 추가 JPA, QueryDSL 관련 설정을 여기서 정의할 수 있습니다.
//    }
//}