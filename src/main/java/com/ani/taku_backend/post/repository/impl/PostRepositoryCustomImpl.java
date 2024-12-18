package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.model.entity.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Post> findPostsWithNoOffset(String filter, Object lastValue, boolean isAsc, int limit) {
        QPost post = QPost.post;

        BooleanExpression condition = getCondition(filter, lastValue, isAsc, post);        // Where 절 조건
        OrderSpecifier<?> orderSpecifier = getSpecifier(filter, isAsc, post);              // 정렬의 기준

        return jpaQueryFactory
                .selectFrom(post)
                .where(condition)
                .orderBy(orderSpecifier)
                .limit(limit)
                .fetch();
    }

    private BooleanExpression getCondition(String filter, Object lastValue, boolean isAsc, QPost post) {

        if ("likes".equalsIgnoreCase(filter) && lastValue != null) {
            return isAsc ? post.likes.gt((Integer) lastValue) : post.likes.lt((Integer) lastValue);

        } else if ("views".equalsIgnoreCase(filter) && lastValue != null) {
            return isAsc ? post.views.gt((Integer) lastValue) : post.views.lt((Integer) lastValue);

        } else if (lastValue != null) {
            return isAsc ? post.createdAt.gt((LocalDateTime) lastValue) : post.createdAt.lt((LocalDateTime) lastValue);

        }
        return null;
    }

    private OrderSpecifier<?> getSpecifier(String filter, Object lastValue, QPost post) {
        if ("likes".equalsIgnoreCase(filter)) {
            return post.likes.desc();
        } else if ("views".equalsIgnoreCase(filter)) {
            return post.views.desc();
        }
        return post.createdAt.desc();
    }
}
