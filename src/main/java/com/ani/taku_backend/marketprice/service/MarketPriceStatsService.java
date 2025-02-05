package com.ani.taku_backend.marketprice.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.ani.taku_backend.marketprice.model.dto.PriceGraphResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.SimilarProductResponseDTO;
import com.ani.taku_backend.marketprice.model.dto.WeeklyStatsResponseDTO;
import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import com.ani.taku_backend.marketprice.repository.CompletedDealRepository;
import com.ani.taku_backend.marketprice.repository.MarketPriceStatsRepository;
import com.ani.taku_backend.marketprice.util.batch.TfidfService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketPriceStatsService {

    private final MarketPriceStatsRepository marketPriceStatsRepository;
    private final CompletedDealRepository completedDealRepository;
    private final ItemCategoriesRepository itemCategoriesRepository;
    private final TfidfService tfidfService;
    private final ExtractKeywordService extractKeywordService;
    private static final Logger log = LoggerFactory.getLogger(MarketPriceStatsService.class);

    /**
     * priceGraph 캐시를 위한 메서드.
     * 캐시 key에 fromDate, toDate, option 등을 문자열로 덧붙여 세분화.
     */
    @Cacheable(
            value = "priceGraph",
            key = "T(String).valueOf(#keyword != null ? #keyword : 'NULL_KEY') + '_' + " +
                    "(#fromDate != null ? #fromDate.toString() : 'NULL_FROM') + '_' + " +
                    "(#toDate != null ? #toDate.toString() : 'NULL_TO') + '_' + " +
                    "(#option != null ? #option.name() : 'NULL_OPTION')"
    )
    public PriceGraphResponseDTO getPriceGraph(
            String keyword, LocalDate fromDate, LocalDate toDate, GraphDisplayOption option) {

        return marketPriceStatsRepository.getPriceGraph(keyword, fromDate, toDate, option);
    }

    @Cacheable(
            value = "weeklyStats",
            key = "T(String).valueOf(#keyword != null ? #keyword : 'NULL_KEYWEEKLY')"
    )
    public WeeklyStatsResponseDTO getWeeklyStats(String keyword) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        return marketPriceStatsRepository.getWeeklyStats(keyword);
    }

    /**
     * 유사 상품 찾기
     */
    public List<SimilarProductResponseDTO> findSimilarProducts(String keyword, Pageable pageable) {
        List<String> extractedKeywords = extractKeywordService.extractKeywords(keyword);
        if (extractedKeywords.isEmpty()) {
            throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<TfidfService.ProductWithSimilarity> similarProducts =
                tfidfService.calculateProductSimilarities(keyword, extractedKeywords);

        return similarProducts.stream()
                .map(productWithSimilarity -> SimilarProductResponseDTO.from(productWithSimilarity))
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

    /**
     * 시세 정보 저장
     */
    @Transactional
    public void saveMarketPriceStats(DuckuJangter product) {
        log.info("시세 정보 저장 시작 - productId: {}, title: {}, price: {}", 
            product.getId(), product.getTitle(), product.getPrice());

        MarketPriceStats stats = MarketPriceStats.builder()
                .product(product)
                .title(product.getTitle())
                .registeredPrice(product.getPrice())
                .registeredDate(LocalDate.now())
                .build();

        MarketPriceStats savedStats = marketPriceStatsRepository.save(stats);
        log.info("시세 정보 저장 완료 - statsId: {}, productId: {}", 
            savedStats.getStatsId(), product.getId());
    }

    /**
     * 거래 완료 시점에 판매가격 업데이트
     */
    @Transactional
    public void updateSoldPrice(DuckuJangter product, BigDecimal soldPrice) {
        log.info("판매 완료 시세 정보 업데이트 시작 - productId: {}, soldPrice: {}", 
            product.getId(), soldPrice);

        // 가장 최근 시세 정보
        MarketPriceStats stats = marketPriceStatsRepository
                .findFirstByProductOrderByRegisteredDateDesc(product)
                .orElseThrow(() -> {
                    log.error("시세 정보를 찾을 수 없음 - productId: {}", product.getId());
                    return new DuckwhoException(ErrorCode.MARKET_PRICE_NOT_FOUND);
                });

        // 시세 정보 업데이트
        stats.updateSoldPrice(soldPrice);
        log.info("시세 정보 업데이트 완료 - statsId: {}, productId: {}, soldPrice: {}", 
            stats.getStatsId(), product.getId(), soldPrice);

        // 카테고리명
        String categoryName = findCategoryByTitle(product.getTitle());

        // 거래 완료 정보 저장
        CompletedDeal completedDeal = CompletedDeal.builder()
                .product(product)
                .marketPriceStats(stats)
                .title(product.getTitle())
                .price(soldPrice)
                .categoryName(categoryName)
                .searchKeywords(String.join(" ", extractKeywordService.extractKeywords(product.getTitle())))
                .build();

        completedDealRepository.save(completedDeal);
    }

    private String findCategoryByTitle(String title) {
        return itemCategoriesRepository.findAll().stream()
                .filter(category -> title.toLowerCase().contains(category.getName().toLowerCase()))
                .findFirst()
                .map(ItemCategories::getName)
                .orElse("미분류");
    }
}