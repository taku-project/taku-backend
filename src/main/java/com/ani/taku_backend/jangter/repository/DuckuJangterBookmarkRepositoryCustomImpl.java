package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.model.entity.QImage;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.QDuckuJangter;
import com.ani.taku_backend.jangter.model.entity.QDuckuJangterBookmark;
import com.ani.taku_backend.jangter.model.entity.QItemCategories;
import com.ani.taku_backend.jangter.model.entity.QJangterImages;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DuckuJangterBookmarkRepositoryCustomImpl implements DuckuJangterBookmarkRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BookmarkListResponseDTO> findBookmarksByUserIdWithPaging(Long userId, Long categoryId, Pageable pageable) {
        QDuckuJangterBookmark bookmark = QDuckuJangterBookmark.duckuJangterBookmark;
        QDuckuJangter jangter = QDuckuJangter.duckuJangter;
        QItemCategories category = QItemCategories.itemCategories;
        QJangterImages jangterImage = QJangterImages.jangterImages;
        QImage image = QImage.image;

        JPAQuery<BookmarkListResponseDTO> query = queryFactory
                .select(Projections.constructor(BookmarkListResponseDTO.class,
                        jangter.id,
                        jangter.title,
                        jangter.price,
                        JPAExpressions
                                .select(jangterImage.image.imageUrl)
                                .from(jangterImage)
                                .where(jangterImage.duckuJangter.eq(jangter))
                                .orderBy(jangterImage.id.asc())
                                .limit(1),
                        jangter.user.nickname,
                        jangter.viewCount,
                        category.id,
                        category.name,
                        bookmark.createdAt
                ))
                .from(bookmark)
                .join(bookmark.jangter, jangter)
                .join(jangter.itemCategories, category)
                .join(jangter.user)
                .where(
                        bookmark.bookmark.user.userId.eq(userId),
                        categoryIdEquals(categoryId),
                        jangter.deletedAt.isNull()  // 삭제되지 않은 상품만 조회
                );

        // 정렬 적용
        OrderSpecifier<?>[] orders = getOrderSpecifier(pageable.getSort());
        query.orderBy(orders);

        // 카운트 쿼리
        long total = queryFactory
                .select(bookmark.count())
                .from(bookmark)
                .join(bookmark.jangter, jangter)
                .join(jangter.itemCategories, category)
                .where(
                        bookmark.bookmark.user.userId.eq(userId),
                        categoryIdEquals(categoryId),
                        jangter.deletedAt.isNull()
                )
                .fetchOne();

        List<BookmarkListResponseDTO> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression categoryIdEquals(Long categoryId) {
        if (categoryId == null || categoryId == 0) {
            return null;
        }
        return QDuckuJangterBookmark.duckuJangterBookmark.jangter.itemCategories.id.eq(categoryId);
    }

    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        QDuckuJangterBookmark bookmark = QDuckuJangterBookmark.duckuJangterBookmark;
        QDuckuJangter jangter = QDuckuJangter.duckuJangter;

        if (sort.isEmpty()) {
            return new OrderSpecifier[]{new OrderSpecifier<>(Order.DESC, bookmark.createdAt)};
        }

        Map<String, Expression<?>> mapping = new HashMap<>();
        mapping.put("price", jangter.price);
        mapping.put("title", jangter.title);
        mapping.put("viewCount", jangter.viewCount);
        mapping.put("createdAt", bookmark.createdAt);

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        sort.forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            Expression<?> expression = mapping.get(order.getProperty());
            if (expression != null) {
                orders.add(new OrderSpecifier(direction, expression));
            } else {
                orders.add(new OrderSpecifier<>(Order.DESC, bookmark.createdAt));
            }
        });

        return orders.toArray(new OrderSpecifier[0]);
    }
}