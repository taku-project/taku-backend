package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO.PriceDataPoint;
import com.ani.taku_backend.marketprice.model.entity.QMarketPriceStats;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MarketPriceStatsQueryRepositoryImpl implements MarketPriceStatsQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QMarketPriceStats stats = QMarketPriceStats.marketPriceStats;

    @Override
    public PriceGraphResponseDTO getPriceGraph(
            String title, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option) {

        List<PriceDataPoint> dataPoints = new ArrayList<>();

        // 등록가 조회
        if (option == GraphDisplayOption.REGISTERED_PRICE_ONLY || option == GraphDisplayOption.ALL) {
            List<Tuple> registeredResults = queryFactory
                    .select(
                            stats.registeredDate,
                            stats.registeredPrice.avg(),
                            stats.registeredDate.count()
                    )
                    .from(stats)
                    .where(
                            stats.registeredDate.between(fromDate, toDate),
                            stats.title.contains(title)
                    )
                    .groupBy(stats.registeredDate)
                    .orderBy(stats.registeredDate.asc())
                    .fetch();

            dataPoints.addAll(registeredResults.stream()
                    .map(tuple -> PriceDataPoint.builder()
                            .date(tuple.get(stats.registeredDate))
                            .registeredPrice(BigDecimal.valueOf(tuple.get(stats.registeredPrice.avg())))
                            .dealCount(tuple.get(stats.registeredDate.count()).intValue())
                            .build())
                    .collect(Collectors.toList()));
        }

        // 판매가 조회
        if (option == GraphDisplayOption.SOLD_PRICE_ONLY || option == GraphDisplayOption.ALL) {
            List<Tuple> soldResults = queryFactory
                    .select(
                            stats.registeredDate,
                            stats.soldPrice.avg(),
                            stats.registeredDate.count()
                    )
                    .from(stats)
                    .where(
                            stats.registeredDate.between(fromDate, toDate),
                            stats.title.contains(title),
                            stats.soldPrice.isNotNull()
                    )
                    .groupBy(stats.registeredDate)
                    .orderBy(stats.registeredDate.asc())
                    .fetch();

            // 기존 데이터와 병합
            Map<LocalDate, PriceDataPoint> mergedData = dataPoints.stream()
                    .collect(Collectors.toMap(
                            PriceDataPoint::getDate,
                            point -> point
                    ));

            soldResults.forEach(tuple -> {
                LocalDate date = tuple.get(stats.registeredDate);
                BigDecimal soldPrice = BigDecimal.valueOf(tuple.get(stats.soldPrice.avg()));
                int count = tuple.get(stats.registeredDate.count()).intValue();

                mergedData.compute(date, (k, v) -> {
                    if (v == null) {
                        return PriceDataPoint.builder()
                                .date(date)
                                .soldPrice(soldPrice)
                                .dealCount(count)
                                .build();
                    } else {
                        return PriceDataPoint.builder()
                                .date(date)
                                .registeredPrice(v.getRegisteredPrice())
                                .soldPrice(soldPrice)
                                .dealCount(v.getDealCount() + count)
                                .build();
                    }
                });
            });

            dataPoints = new ArrayList<>(mergedData.values());
        }

        return PriceGraphResponseDTO.builder()
                .dataPoints(dataPoints)
                .build();
    }
}