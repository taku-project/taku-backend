package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
import com.ani.taku_backend.jangter.model.dto.ProductViewAndBookmarkDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.FindRecommendFilteredProductsRequestDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductFindListRequestDTO;
import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.*;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.ani.taku_backend.user.model.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.common.model.entity.QImage.image;
import static com.ani.taku_backend.jangter.model.entity.QDuckuJangter.duckuJangter;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DuckuJangterRepositoryCustomImpl implements DuckuJangterRepositoryCustom{

    private final JPAQueryFactory queryFactory;


    @Override
    public List<ProductFindListResponseDTO> findFilteredProducts(
            ProductFindListRequestDTO request
           ) {

        String keyword = request.getSearchKeyword();
        Long categoryId = request.getCategoryId();
        Integer minPrice = request.getMinPrice();
        Integer maxPrice  = request.getMaxPrice();
        String sort = request.getSort();
        String order = request.getOrder();
        Long lastId = request.getLastId();
        int limit = request.getSize();

        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        QUser user = QUser.user;
        QItemCategories itemCategories = QItemCategories.itemCategories;
        QJangterImages jangterImages = QJangterImages.jangterImages;

        var query = queryFactory.select(Projections.constructor(
                        ProductFindListResponseDTO.class,
                        duckuJangter.id,
                        duckuJangter.title,
                        duckuJangter.price,
                        jangterImages.image.imageUrl, // 이미지 컬렉션을 조인한 후 첫 번째 이미지 URL을 가져옴
                        duckuJangter.user.nickname,
                        duckuJangter.viewCount
                ))
                .from(duckuJangter)
                .leftJoin(duckuJangter.jangterImages, jangterImages)
                .leftJoin(jangterImages.image, image)
                .join(duckuJangter.user, user)
                .join(duckuJangter.itemCategories, itemCategories)
                 // left join을 사용하여 이미지 컬렉션을 안전하게 가져옴
                .where(
                        duckuJangter.deletedAt.isNull(),
                        applyFilters(keyword, categoryId, minPrice, maxPrice),
                        applyPaginationCondition(sort, order, lastId))
                .orderBy(buildOrder(sort,order))
                .limit(limit)
                .fetch();


        // 페이지네이션
        return query;
    }

    private BooleanExpression applyFilters(String keyword, Long categoryId, Integer minPrice, Integer maxPrice) {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;

        BooleanExpression predicate = duckuJangter.deletedAt.isNull(); // 기본 조건

        if (StringUtils.hasText(keyword)) {
            predicate = predicate.and(
                    duckuJangter.title.containsIgnoreCase(keyword)
                            .or(duckuJangter.description.containsIgnoreCase(keyword))
            );
        }

        if(categoryId==0){}else{
            predicate = predicate.and(duckuJangter.itemCategories.id.eq(categoryId));

        }

        if (minPrice == null){}else{

            predicate = predicate.and(duckuJangter.price.goe(minPrice));
        }

        if (maxPrice == null){}else {
            predicate = predicate.and(duckuJangter.price.loe(maxPrice));
        }

        return predicate;
    }



    private BooleanExpression applyPaginationCondition(String sort, String order, Long lastId) {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        Order sortOrder = "asc".equalsIgnoreCase(order) ? Order.ASC : Order.DESC;

        if (lastId == null) {
            return null; // 첫 페이지 요청의 경우 조건 없음
        }

        if ("price".equalsIgnoreCase(sort)) {
            BigDecimal lastPrice = queryFactory
                    .select(duckuJangter.price)
                    .from(duckuJangter)
                    .where(duckuJangter.id.eq(lastId))
                    .fetchOne();



            // 같은 price인 경우 id로 정렬
            if (sortOrder == Order.ASC) {
                if (lastPrice == null) {
                    lastPrice = BigDecimal.ZERO;
                }
                return duckuJangter.price.gt(lastPrice)
                        .or(duckuJangter.price.eq(lastPrice)
                                .and(duckuJangter.id.gt(lastId)));
            } else {
                if (lastPrice == null) {
                    lastPrice = new BigDecimal(Long.MAX_VALUE);
                }
                return duckuJangter.price.lt(lastPrice)
                        .or(duckuJangter.price.eq(lastPrice)
                                .and(duckuJangter.id.gt(lastId)));
            }
        } else if ("day".equalsIgnoreCase(sort)) {
            var lastCreatedAt = queryFactory
                    .select(duckuJangter.createdAt)
                    .from(duckuJangter)
                    .where(duckuJangter.id.eq(lastId))
                    .fetchOne();

            if (lastCreatedAt == null) {
                return null; // 날짜 정보가 없으면 페이지네이션 불가
            }

            // 같은 createdAt인 경우 id로 정렬
            if (sortOrder == Order.ASC) {
                return duckuJangter.createdAt.gt(lastCreatedAt)
                        .or(duckuJangter.createdAt.eq(lastCreatedAt)
                                .and(duckuJangter.id.gt(lastId)));
            } else {
                return duckuJangter.createdAt.lt(lastCreatedAt)
                        .or(duckuJangter.createdAt.eq(lastCreatedAt)
                                .and(duckuJangter.id.gt(lastId)));
            }
        }

        return null; // 기본 조건
    }


    private OrderSpecifier<?>[] buildOrder(String sort, String order) {
        // 동적으로 정렬 조건을 생성
        List<OrderSpecifier<?>> orders = new ArrayList<>();

            switch (sort+order) {
                case "priceasc":
                    orders.add(new OrderSpecifier<>(Order.ASC, duckuJangter.price));
                    break;
                case "pricedesc":
                    orders.add(new OrderSpecifier<>(Order.DESC, duckuJangter.price));
                    break;
                case "dayasc":
                    orders.add(new OrderSpecifier<>(Order.ASC, duckuJangter.createdAt));
                    break;
                case "daydesc":
                    orders.add(new OrderSpecifier<>(Order.DESC, duckuJangter.createdAt));
                    break;
                default:
                    throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
            }

        orders.add(new OrderSpecifier<>(Order.ASC, duckuJangter.id));

        return orders.toArray(new OrderSpecifier[0]);
    }


    @Override
    public List<DuckuJangter> findRecommendFilteredProducts(FindRecommendFilteredProductsRequestDTO request) {

        List<String> keywords = request.getKeywords();
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();
        Long itemCategoryId = request.getItemCategoryId();
        ProductStatus status = request.getStatus();
        Long productId = request.getProductId();
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;

        BooleanBuilder titleConditions = new BooleanBuilder();
        keywords.forEach(keyword ->
                titleConditions.or(duckuJangter.title.containsIgnoreCase(keyword))
        );

        List<DuckuJangter> fetch = this.queryFactory.selectFrom(duckuJangter)
                .where(
                        titleConditions,
                        duckuJangter.price.between(minPrice, maxPrice),
                        duckuJangter.itemCategories.id.eq(itemCategoryId),
                        duckuJangter.status.eq(status),
                        duckuJangter.id.ne(productId)
                )
                .distinct()
                .fetch();

        log.info("fetch : {}", fetch);

        return fetch;
    }

    @Override
    public List<CategoryGroupCountDTO> findCategoryGroupCount() {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        QItemCategories itemCategories = QItemCategories.itemCategories;

        return queryFactory
                .select(Projections.constructor(CategoryGroupCountDTO.class,
                        itemCategories.id,
                        itemCategories.name,
                        duckuJangter.count()))
                .from(duckuJangter)
                .leftJoin(duckuJangter.itemCategories, itemCategories)
                .where(duckuJangter.deletedAt.isNull())
                .groupBy(itemCategories.id, itemCategories.name)
                .orderBy(duckuJangter.count().desc())
                .fetch();
    }

    @Override
    public List<ProductViewAndBookmarkDTO> findProductViewAndBookmark(Long categoryId) {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        QDuckuJangterBookmark duckuJangterBookmark = QDuckuJangterBookmark.duckuJangterBookmark;

        return queryFactory
                .select(Projections.constructor(ProductViewAndBookmarkDTO.class,
                        duckuJangter.id,
                        duckuJangter.viewCount,
                        duckuJangterBookmark.isNotNull()))
                .from(duckuJangter)
                .leftJoin(duckuJangterBookmark)
                .on(duckuJangter.id.eq(duckuJangterBookmark.jangter.id))
                .where(duckuJangter.status.eq(ProductStatus.FOR_SALE),
                        duckuJangter.itemCategories.id.eq(categoryId))
                .fetch();
    }

    @Override
    public List<ProductViewAndBookmarkDTO> findProductViewAndBookmarkByProductId(Long productId) {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        QDuckuJangterBookmark duckuJangterBookmark = QDuckuJangterBookmark.duckuJangterBookmark;

        return queryFactory
                .select(Projections.constructor(ProductViewAndBookmarkDTO.class,
                        duckuJangter.id,
                        duckuJangter.viewCount,
                        duckuJangterBookmark.isNotNull()))
                .from(duckuJangter)
                .leftJoin(duckuJangterBookmark)
                .on(duckuJangter.id.eq(duckuJangterBookmark.jangter.id))
                .where(duckuJangter.id.eq(productId) , duckuJangter.deletedAt.isNull() , duckuJangter.status.eq(ProductStatus.FOR_SALE))
                .fetch();
    }


}
