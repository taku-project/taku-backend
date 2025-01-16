package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDto;
import com.ani.taku_backend.jangter.model.entity.QDuckuJangter;
import com.ani.taku_backend.jangter.model.entity.QItemCategories;
import com.ani.taku_backend.jangter.model.entity.QJangterImages;
import com.ani.taku_backend.user.model.entity.QUser;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.common.model.entity.QImage.image;
import static com.ani.taku_backend.jangter.model.entity.QDuckuJangter.duckuJangter;

@RequiredArgsConstructor
public class DuckuJangterRepositoryImpl implements DuckuJangterRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductFindListResponseDto> findFilteredProducts(
            String keyword,
            String category,
            Integer minPrice,
            Integer maxPrice,
            String sort,
            String order,
            Long lastId,
            int limit) {

        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        QUser user = QUser.user;
        QItemCategories itemCategories = QItemCategories.itemCategories;
        QJangterImages jangterImages = QJangterImages.jangterImages;

        System.out.println(keyword+category+minPrice+maxPrice+sort+order+lastId+limit);
        var query = queryFactory.select(Projections.constructor(
                        ProductFindListResponseDto.class,
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
                        applyFilters(keyword, category, minPrice, maxPrice),
                        applyPaginationCondition(sort, order, lastId))
                .orderBy(buildOrder(sort,order));


        // 페이지네이션
        return query.limit(limit).fetch();
    }

    private BooleanExpression applyFilters(String keyword, String category, Integer minPrice, Integer maxPrice) {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;

        BooleanExpression predicate = duckuJangter.deletedAt.isNull(); // 기본 조건

        if (StringUtils.hasText(keyword)) {
            predicate = predicate.and(
                    duckuJangter.title.containsIgnoreCase(keyword)
                            .or(duckuJangter.description.containsIgnoreCase(keyword))
            );
        }

        System.out.println("category"+category);
        System.out.println(category.equals(""));
        if (!category.equals("")) {
            System.out.println("category 조건 추가");
            predicate = predicate.and(duckuJangter.itemCategories.name.eq(category));
        }

        if (minPrice != null) {
            predicate = predicate.and(duckuJangter.price.goe(minPrice));
        }

        if (maxPrice != null) {
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
                    throw new IllegalArgumentException("Unknown order: " + order);
            }

        orders.add(new OrderSpecifier<>(Order.ASC, duckuJangter.id));

        return orders.toArray(new OrderSpecifier[0]);
    }

}
