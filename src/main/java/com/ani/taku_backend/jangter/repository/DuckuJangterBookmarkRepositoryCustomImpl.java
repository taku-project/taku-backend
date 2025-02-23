package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.model.entity.QDuckuJangter;
import com.ani.taku_backend.jangter.model.entity.QDuckuJangterBookmark;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.Expression;
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
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DuckuJangterBookmarkRepositoryCustomImpl implements DuckuJangterBookmarkRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BookmarkListResponseDTO> findBookmarksByUserIdWithPaging(Long userId, Long categoryId, Pageable pageable) {
        QDuckuJangterBookmark bookmark = QDuckuJangterBookmark.duckuJangterBookmark;
        QDuckuJangter jangter = QDuckuJangter.duckuJangter;

        JPAQuery<DuckuJangterBookmark> query = queryFactory
                .selectFrom(bookmark)
                .leftJoin(bookmark.jangter, jangter).fetchJoin()
                .where(
                        bookmark.bookmark.user.userId.eq(userId),
                        categoryIdEquals(categoryId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        OrderSpecifier<?>[] orders = getOrderSpecifier(pageable.getSort());
        query.orderBy(orders);

        List<DuckuJangterBookmark> bookmarks = query.fetch();
        long total = query.fetchCount();

        List<BookmarkListResponseDTO> dtos = bookmarks.stream()
                .map(BookmarkListResponseDTO::from)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public List<BookmarkListResponseDTO> findBookmarksByUserIdAndCategoryId(Long userId, Long categoryId) {
        QDuckuJangterBookmark bookmark = QDuckuJangterBookmark.duckuJangterBookmark;
        QDuckuJangter jangter = QDuckuJangter.duckuJangter;

        List<DuckuJangterBookmark> bookmarks = queryFactory
                .selectFrom(bookmark)
                .leftJoin(bookmark.jangter, jangter).fetchJoin()
                .where(
                        bookmark.bookmark.user.userId.eq(userId),
                        categoryIdEquals(categoryId)
                )
                .orderBy(bookmark.createdAt.desc())
                .fetch();

        return bookmarks.stream()
                .map(BookmarkListResponseDTO::from)
                .collect(Collectors.toList());
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
        mapping.put("view_count", jangter.viewCount);
        mapping.put("created_at", bookmark.createdAt);

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        sort.forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            Expression<?> expression = mapping.get(order.getProperty().toLowerCase());
            if (expression != null) {
                orders.add(new OrderSpecifier(direction, expression));
            } else {
                orders.add(new OrderSpecifier<>(Order.DESC, bookmark.createdAt));
            }
        });
        return orders.toArray(new OrderSpecifier[0]);
    }
}