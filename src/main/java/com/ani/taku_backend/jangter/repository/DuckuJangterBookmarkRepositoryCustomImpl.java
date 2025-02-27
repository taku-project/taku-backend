package com.ani.taku_backend.jangter.repository;

import static com.ani.taku_backend.category.domain.entity.QCategory.category;

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
import com.querydsl.core.types.ExpressionUtils;

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
        QJangterImages jangterImage = QJangterImages.jangterImages;

        // 메인 쿼리 - user_id 필드 직접 참조
        JPAQuery<BookmarkListResponseDTO> query = queryFactory
            .select(Projections.constructor(BookmarkListResponseDTO.class,
                jangter.id,
                jangter.title,
                jangter.price,
                jangter.viewCount,
                jangter.itemCategories.id,
                ExpressionUtils.as(
                    JPAExpressions
                        .select(jangterImage.image.imageUrl)
                        .from(jangterImage)
                        .leftJoin(jangterImage.image)
                        .where(
                            jangterImage.duckuJangter.eq(jangter),
                            jangterImage.image.deletedAt.isNull()
                        )
                        .orderBy(jangterImage.id.asc())
                        .limit(1L)
                        .groupBy(jangterImage.duckuJangter),
                    "imageUrl"
                )
            ))
            .from(bookmark)
            .join(bookmark.jangter, jangter)
            .where(
                bookmark.user.userId.eq(userId),
                bookmark.isActive.isTrue(),
                categoryIdEq(categoryId),
                jangter.deletedAt.isNull()
            )
            .orderBy(bookmark.createdAt.desc());

        // 카운트 쿼리 - 동일하게 user_id 직접 참조
        long total = queryFactory
            .select(bookmark.count())
            .from(bookmark)
            .join(bookmark.jangter, jangter)
            .where(
                bookmark.user.userId.eq(userId),
                bookmark.isActive.isTrue(),
                categoryIdEq(categoryId),
                jangter.deletedAt.isNull()
            )
            .fetchOne();

        // 페이징 적용
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

    private BooleanExpression categoryIdEq(Long categoryId) {
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