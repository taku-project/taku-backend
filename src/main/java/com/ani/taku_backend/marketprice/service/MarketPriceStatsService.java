package com.ani.taku_backend.marketprice.service;

import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import com.ani.taku_backend.marketprice.repository.MarketPriceStatsRepository;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarketPriceStatsService {
    private final MarketPriceStatsRepository marketPriceStatsRepository;
    private final TfidfService tfidfService;
    private final ExtractKeywordService extractKeywordService;

    @Transactional(readOnly = true)
    @Cacheable(value = "priceGraph", key = "#keyword + #fromDate + #toDate + #option")
    public PriceGraphResponseDTO getPriceGraph(
            String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option) {
        return marketPriceStatsRepository.getPriceGraph(keyword, fromDate, toDate, option);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "weeklyStats", key = "#keyword")
    public WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        return marketPriceStatsRepository.getWeeklyStats(keyword, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<SimilarProductResponseDTO> findSimilarProducts(String keyword, Pageable pageable) {
        // TF-IDF 벡터 기반 유사도 계산
        List<TfidfService.ProductWithSimilarity> similarProducts =
                tfidfService.calculateProductSimilarities(keyword,
                        extractKeywordService.extractKeywords(keyword));

        // 상위 N개 상품 반환
        return similarProducts.stream()
                .map(SimilarProductResponseDTO::from)
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveMarketPriceStats(DuckuJangter product) {
        MarketPriceStats stats = MarketPriceStats.builder()
                .product(product)
                .title(product.getTitle())
                .registeredPrice(product.getPrice())
                .registeredDate(LocalDate.now())
                .build();

        marketPriceStatsRepository.save(stats);
    }

    @Transactional
    public void updateSoldPrice(DuckuJangter product, BigDecimal soldPrice) {
        marketPriceStatsRepository.findFirstByProductOrderByRegisteredDateDesc(product)
                .ifPresent(stats -> stats.updateSoldPrice(soldPrice));
    }
}