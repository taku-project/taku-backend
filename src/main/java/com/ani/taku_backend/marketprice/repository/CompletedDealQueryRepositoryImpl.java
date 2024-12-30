package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.QCompletedDeal;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class CompletedDealQueryRepositoryImpl implements CompletedDealQueryRepository {
    private final JPAQueryFactory queryFactory;

    public PriceGraphResponseDTO getPriceGraph(String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option) {
        QCompletedDeal deal = QCompletedDeal.completedDeal;

        // 날짜별 가격 통계 조회
        List<Tuple> results = queryFactory
                .select(
                        deal.createdAt.as("date"),
                        deal.price.avg().as("registeredPrice"),  // 등록가
                        deal.price.avg().as("soldPrice"),        // 판매가
                        deal.count().as("dealCount")
                )
                .from(deal)
                .where(
                        deal.searchKeywords.like("%" + keyword + "%")
                                .and(deal.createdAt.between(
                                        fromDate.atStartOfDay(),
                                        toDate.atTime(23, 59, 59)))
                )
                .groupBy(deal.createdAt)
                .orderBy(deal.createdAt.asc())
                .fetch();

        // PriceDataPoint로 변환
        List<PriceGraphResponseDTO.PriceDataPoint> dataPoints = results.stream()
                .map(tuple -> PriceGraphResponseDTO.PriceDataPoint.builder()
                        .date(tuple.get(deal.createdAt).toLocalDate())
                        .registeredPrice(tuple.get(1, BigDecimal.class))
                        .soldPrice(tuple.get(2, BigDecimal.class))
                        .dealCount(tuple.get(3, Integer.class))
                        .build())
                .collect(Collectors.toList());

        return PriceGraphResponseDTO.builder()
                .dataPoints(dataPoints)
                .build();
    }

    @Override
    public WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
        QCompletedDeal deal = QCompletedDeal.completedDeal;
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);

        return queryFactory
                .select(Projections.constructor(WeeklyStatsResponseDTO.class,
                        deal.price.avg(),
                        deal.price.max(),
                        deal.price.min(),
                        deal.count()
                ))
                .from(deal)
                .where(
                        deal.searchKeywords.like("%" + keyword + "%")
                                .and(deal.createdAt.after(weekAgo))
                )
                .fetchOne();
    }

    @Override
    public List<SimilarProductResponseDTO> findSimilarProducts(String keyword, Pageable pageable) {
        QCompletedDeal deal = QCompletedDeal.completedDeal;

        return queryFactory
                .select(Projections.constructor(SimilarProductResponseDTO.class,
                        deal.id,
                        deal.title,
                        deal.price,
                        deal.similarity
                ))
                .from(deal)
                .where(deal.searchKeywords.like("%" + keyword + "%"))
                .orderBy(deal.similarity.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}