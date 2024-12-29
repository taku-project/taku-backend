package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.QCompletedDeal;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
                        deal.price.avg().as("avgPrice"),
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

        return PriceGraphResponseDTO.builder()
                .keyword(keyword)
                .fromDate(fromDate)
                .toDate(toDate)
                .displayOption(option)
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