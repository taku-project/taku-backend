package com.ani.taku_backend.jangter.util.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.ani.taku_backend.common.enums.PeriodType;
import com.ani.taku_backend.common.enums.RankType;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
import com.ani.taku_backend.jangter.model.dto.ProductScoreDTO;
import com.ani.taku_backend.jangter.model.dto.ProductViewAndBookmarkDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.rank.JangterRankBase;
import com.ani.taku_backend.jangter.model.entity.rank.JangterRankStats;
import com.ani.taku_backend.jangter.model.entity.rank.JangterRankType;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.JangterRankBaseRepository;
import com.ani.taku_backend.jangter.repository.JangterRankTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JangterRankBatchService {

    private final DuckuJangterRepository duckuJangterRepository;

    private final JangterRankTypeRepository jangterRankTypeRepository;
    private final JangterRankBaseRepository jangterRankBaseRepository;
    
    /**
     * 장터 일별 랭킹 생성
     */
    public void createJangterDailyRank() {
        List<JangterRankType> jangterRankTypes = jangterRankTypeRepository.findAll();
        log.info("장터 랭킹 조회 완료 : {}", jangterRankTypes);

        Map<RankType, JangterRankType> rankTypeMap = jangterRankTypes.stream()
            .filter(rankType -> rankType.getStatus() == StatusType.ACTIVE)
            .collect(Collectors.toMap(
                JangterRankType::getType,
                rankType -> rankType
            ));

        JangterRankType bookmarkType = Optional.ofNullable(rankTypeMap.get(RankType.BOOKMARK))
            .orElseThrow(() -> new IllegalArgumentException("북마크 랭킹 타입 조회 실패"));
        JangterRankType viewType = Optional.ofNullable(rankTypeMap.get(RankType.VIEW))
            .orElseThrow(() -> new IllegalArgumentException("조회수 랭킹 타입 조회 실패"));

        log.info("북마크 랭킹 조회 완료 : {}", bookmarkType);
        log.info("조회수 랭킹 조회 완료 : {}", viewType);

        List<ProductViewAndBookmarkDTO> allProducts = getCategoryGroupCount();


        long maxViewCount = getMaxViewCount(allProducts);

        // 스코어링
        List<ProductScoreDTO> rankScores = scoreRanking(allProducts , viewType , bookmarkType , maxViewCount);

        // 랭킹 부여
        setRank(rankScores);

        log.info("스코어링 완료 : {}", rankScores);

        // 랭킹 저장
        List<JangterRankBase> jangterRankBases = saveRank(rankScores , viewType , bookmarkType , PeriodType.DAY , 
            LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0) , 
            LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999) , 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
        // 트랜잭션 내에서 저장
        jangterRankBaseRepository.saveAll(jangterRankBases);
    }

    /**
     * 장터 주간 랭킹 생성
     */
    public void createJangterWeeklyRank() {

        List<JangterRankType> jangterRankTypes = jangterRankTypeRepository.findAll();
        Map<RankType, JangterRankType> rankTypeMap = jangterRankTypes.stream()
        .filter(rankType -> rankType.getStatus() == StatusType.ACTIVE)
        .collect(Collectors.toMap(
            JangterRankType::getType,
            rankType -> rankType
        ));

        JangterRankType bookmarkType = Optional.ofNullable(rankTypeMap.get(RankType.BOOKMARK))
            .orElseThrow(() -> new IllegalArgumentException("북마크 랭킹 타입 조회 실패"));
        JangterRankType viewType = Optional.ofNullable(rankTypeMap.get(RankType.VIEW))
            .orElseThrow(() -> new IllegalArgumentException("조회수 랭킹 타입 조회 실패"));


        LocalDateTime now = LocalDateTime.now().minusDays(1);
        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1)  // 1주일 전
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekEnd = now
            .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        // 일주일치 데이터 조회 예시
        List<JangterRankBase> weeklyRanks = jangterRankBaseRepository
            .findByStartDateBetweenAndPeriodTypeOrderByTotalScoreDesc(
                weekStart, 
                weekEnd,
                PeriodType.DAY
            );

        // 일주일치 데이터 조회 후 중복 제거 (높은 점수만 유지)
        List<JangterRankBase> uniqueRanks = weeklyRanks.stream()
            .collect(Collectors.groupingBy(
                rank -> rank.getDuckuJangter().getId(),  // 상품 ID로 그룹핑
                Collectors.maxBy(Comparator.comparing(JangterRankBase::getTotalScore))  // 최고 점수 선택
            ))
            .values()
            .stream()
            .map(Optional::get)
            .sorted(Comparator.comparing(JangterRankBase::getTotalScore).reversed())  // 다시 점수순 정렬
            .collect(Collectors.toList());

        log.info("전체 랭킹 수: {}, 중복 제거 후: {}", weeklyRanks.size(), uniqueRanks.size());

        List<ProductViewAndBookmarkDTO> productViewAndBookmarkDTOs = uniqueRanks.stream().map(rank -> {
            return duckuJangterRepository.findProductViewAndBookmarkByProductId(rank.getDuckuJangter().getId());
        }).flatMap(List::stream).toList();

        long maxViewCount = getMaxViewCount(productViewAndBookmarkDTOs);
        // 스코어링
        List<ProductScoreDTO> rankScores = scoreRanking(productViewAndBookmarkDTOs , viewType , bookmarkType , maxViewCount);

        // 랭킹 부여
        setRank(rankScores);
        // 랭킹 저장


        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int monthWeekNumber = now.get(weekFields.weekOfMonth());         // 월간 주차

        // 2자리 주차 포맷팅 (예: 03)
        String periodKey = String.format("%d-%02d-W%02d", 
            now.getYear(),
            now.getMonthValue(),
            monthWeekNumber
        );

        // 결과: "2025-01-W03"
        log.info("Period Key: {}", periodKey);

        List<JangterRankBase> jangterRankBases = saveRank(rankScores , viewType , bookmarkType , PeriodType.WEEK , 
            weekStart , weekEnd , periodKey);
        jangterRankBaseRepository.saveAll(jangterRankBases);

        log.info("주간 집계 기간: {} ~ {}", 
            weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            weekEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        log.info("장터 랭킹 생성 완료");
    }

    /**
     * 장터 월간 랭킹 생성
     */
    public void createJangterMonthlyRank() {
        List<JangterRankType> jangterRankTypes = jangterRankTypeRepository.findAll();
        Map<RankType, JangterRankType> rankTypeMap = jangterRankTypes.stream()
        .filter(rankType -> rankType.getStatus() == StatusType.ACTIVE)
        .collect(Collectors.toMap(
            JangterRankType::getType,
            rankType -> rankType
        ));

        JangterRankType bookmarkType = Optional.ofNullable(rankTypeMap.get(RankType.BOOKMARK))
            .orElseThrow(() -> new IllegalArgumentException("북마크 랭킹 타입 조회 실패"));
        JangterRankType viewType = Optional.ofNullable(rankTypeMap.get(RankType.VIEW))
            .orElseThrow(() -> new IllegalArgumentException("조회수 랭킹 타입 조회 실패"));


        LocalDateTime now = LocalDateTime.now().minusDays(1);
        LocalDateTime monthStart = LocalDateTime.now().minusMonths(1)  // 1달 전
            .withDayOfMonth(1)  // 해당 월의 1일로 설정
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthEnd = now
            .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        // 한달치 데이터 조회
        List<JangterRankBase> monthlyRanks = jangterRankBaseRepository
            .findByStartDateBetweenAndPeriodTypeOrderByTotalScoreDesc(
                monthStart, 
                monthEnd,
                PeriodType.WEEK
        );

        // 일주일치 데이터 조회 후 중복 제거 (높은 점수만 유지)
        List<JangterRankBase> uniqueRanks = monthlyRanks.stream()
            .collect(Collectors.groupingBy(
                rank -> rank.getDuckuJangter().getId(),  // 상품 ID로 그룹핑
                Collectors.maxBy(Comparator.comparing(JangterRankBase::getTotalScore))  // 최고 점수 선택
            ))
            .values()
            .stream()
            .map(Optional::get)
            .sorted(Comparator.comparing(JangterRankBase::getTotalScore).reversed())  // 다시 점수순 정렬
            .collect(Collectors.toList());

        log.info("전체 랭킹 수: {}, 중복 제거 후: {}", monthlyRanks.size(), uniqueRanks.size());

        List<ProductViewAndBookmarkDTO> productViewAndBookmarkDTOs = uniqueRanks.stream().map(rank -> {
            return duckuJangterRepository.findProductViewAndBookmarkByProductId(rank.getDuckuJangter().getId());
        }).flatMap(List::stream).toList();

        long maxViewCount = getMaxViewCount(productViewAndBookmarkDTOs);
        // 스코어링
        List<ProductScoreDTO> rankScores = scoreRanking(productViewAndBookmarkDTOs , viewType , bookmarkType , maxViewCount);

        // 랭킹 부여
        setRank(rankScores);
        // 랭킹 저장


        // 이전 달의 연도와 월을 가져옴
        LocalDateTime previousMonth = now.minusMonths(1);
        
        // 월간 랭킹의 periodKey 포맷팅 (예: 2024-01)
        String periodKey = String.format("%d-%02d", 
            previousMonth.getYear(),
            previousMonth.getMonthValue()
        );

        // 결과: "2024-01"
        log.info("Period Key: {}", periodKey);

        List<JangterRankBase> jangterRankBases = saveRank(rankScores , viewType , bookmarkType , PeriodType.MONTH , 
            monthStart , monthEnd , periodKey);
        jangterRankBaseRepository.saveAll(jangterRankBases);

        log.info("월간 집계 기간: {} ~ {}", 
            monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            monthEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        log.info("장터 랭킹 생성 완료");
    }


    private List<ProductViewAndBookmarkDTO> getCategoryGroupCount() {
        List<CategoryGroupCountDTO> categoryGroupCount = duckuJangterRepository.findCategoryGroupCount();
        log.info("카테고리 그룹 조회 완료 : {}", categoryGroupCount);
    
        // 병렬처리
        List<ProductViewAndBookmarkDTO> allProducts = categoryGroupCount.parallelStream()
            .map(categoryGroupCountDTO -> 
                duckuJangterRepository.findProductViewAndBookmark(categoryGroupCountDTO.getItemCategoryId())
            )
            .flatMap(List::stream)
            .collect(Collectors.toList());
    
        // 각 카테고리별 조회수 , 북마크 조회
        allProducts.forEach(productViewAndBookmarkDTO -> {
            log.info("카테고리 그룹 조회 완료 : {}", productViewAndBookmarkDTO);
        });
        return allProducts;
    }

    private long getMaxViewCount(List<ProductViewAndBookmarkDTO> allProducts) {
        return allProducts.stream()
            .mapToLong(ProductViewAndBookmarkDTO::getViewCount)
            .max()
            .orElse(1L);  // 기본값 1 (0으로 나누기 방지)
    }

    private List<ProductScoreDTO> scoreRanking(List<ProductViewAndBookmarkDTO> allProducts , JangterRankType viewType , JangterRankType bookmarkType , long maxViewCount) {
        // 병렬 스코어링
        return allProducts.parallelStream()
            .map(product -> {
                // 조회수 스코어 (40%)
                BigDecimal viewScore = BigDecimal.valueOf(product.getViewCount())
                    .multiply(viewType.getWeight())
                    .divide(BigDecimal.valueOf(maxViewCount), 2, RoundingMode.HALF_UP);

                // 북마크 스코어 (60%)
                BigDecimal bookmarkScore = BigDecimal.valueOf(product.getIsBookmarked() ? 1 : 0)
                    .multiply(bookmarkType.getWeight());

                // 총점
                BigDecimal totalScore = viewScore.add(bookmarkScore);

                return new ProductScoreDTO(product, totalScore, viewScore, bookmarkScore);
            })
            .filter(result -> result.getTotalScore().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(ProductScoreDTO::getTotalScore).reversed())
            .limit(30)
            .collect(Collectors.toList());
    }

    private void setRank(List<ProductScoreDTO> rankScores) {
        IntStream.range(0, rankScores.size())
        .forEach(i -> {
            ProductScoreDTO score = rankScores.get(i);
            score.setRank(i + 1);  // 1-based ranking
        });
    }

    private List<JangterRankBase> saveRank(List<ProductScoreDTO> rankScores , JangterRankType viewType , JangterRankType bookmarkType , PeriodType periodType , LocalDateTime startDate , LocalDateTime endDate , String periodKey) {

        List<JangterRankBase> jangterRankBases = rankScores.stream()
        .map(scoreDTO -> {
            // JangterRankBase 생성
            JangterRankBase rankBase = JangterRankBase.builder()
                .totalScore(scoreDTO.getTotalScore())
                .status(StatusType.ACTIVE)
                .rankIdx(scoreDTO.getRank())
                .duckuJangter(DuckuJangter.reference(scoreDTO.getProductId()))
                .periodType(periodType)
                .startDate(startDate)
                .endDate(endDate)
                .periodKey(periodKey)
                .build();

            // JangterRankStats 생성 및 연결
            JangterRankStats viewStats = JangterRankStats.builder()
                .jangterRankBase(rankBase)
                .jangterRankType(viewType)
                .score(scoreDTO.getViewScore())
                .build();

            JangterRankStats bookmarkStats = JangterRankStats.builder()
                .jangterRankBase(rankBase)
                .jangterRankType(bookmarkType)
                .score(scoreDTO.getBookmarkScore())
                .build();

            // rankBase에 stats 연결
            rankBase.addRankStats(Arrays.asList(viewStats, bookmarkStats));

            return rankBase;
        })
        .collect(Collectors.toList());

        return jangterRankBases;
    }
    
}

