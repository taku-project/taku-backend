package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.jangter.model.entity.QDuckuJangter;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.QMarketPriceStats;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CompletedDealQueryRepositoryImpl implements CompletedDealQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public PriceGraphResponseDTO getPriceGraph(String keyword, LocalDate fromDate, LocalDate toDate,
                                               GraphDisplayOption option) {
        QMarketPriceStats stats = QMarketPriceStats.marketPriceStats;

        List<Tuple> results = queryFactory
                .select(
                        stats.registeredDate,
                        stats.registeredPrice.avg(),
                        stats.soldPrice.avg(),
                        stats.registeredDate.count()
                )
                .from(stats)
                .where(
                        stats.title.contains(keyword),
                        stats.registeredDate.between(fromDate, toDate)
                )
                .groupBy(stats.registeredDate)
                .orderBy(stats.registeredDate.asc())
                .fetch();

        List<PriceGraphResponseDTO.PriceDataPoint> dataPoints = results.stream()
                .map(tuple -> PriceGraphResponseDTO.PriceDataPoint.builder()
                        .date(tuple.get(stats.registeredDate))
                        .registeredPrice(BigDecimal.valueOf(tuple.get(stats.registeredPrice.avg())))
                        .soldPrice(BigDecimal.valueOf(tuple.get(stats.soldPrice.avg())))
                        .dealCount(tuple.get(stats.registeredDate.count()).intValue())
                        .build())
                .collect(Collectors.toList());

        return PriceGraphResponseDTO.builder()
                .dataPoints(dataPoints)
                .build();
    }

    @Override
    public WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
        QMarketPriceStats stats = QMarketPriceStats.marketPriceStats;
        LocalDate weekAgo = LocalDate.now().minusWeeks(1);

        return queryFactory
                .select(Projections.constructor(WeeklyStatsResponseDTO.class,
                        stats.registeredPrice.avg(),
                        stats.registeredPrice.max(),
                        stats.registeredPrice.min(),
                        stats.registeredDate.count()
                ))
                .from(stats)
                .where(
                        stats.title.contains(keyword),
                        stats.registeredDate.goe(weekAgo)
                )
                .fetchOne();
    }

    @Override
    public List<SimilarProductResponseDTO> findSimilarProducts(String keyword, Pageable pageable) {
        QDuckuJangter jangter = QDuckuJangter.duckuJangter;

        return queryFactory
                .select(Projections.constructor(
                        SimilarProductResponseDTO.class,
                        jangter.id,
                        jangter.title,
                        jangter.price,
                        // 인자 4: String (tfidfVector)
                        jangter.tfidfVector,
                        jangter.jangterImages.any().image.imageUrl
                ))
                .from(jangter)
                .where(
                        jangter.title.contains(keyword),
                        jangter.deletedAt.isNull()
                )
                .orderBy(jangter.id.desc())  // 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}