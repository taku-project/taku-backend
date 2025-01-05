package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.SortFilterType;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.model.entity.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.ani.taku_backend.common.model.entity.QImage.image;
import static com.ani.taku_backend.post.model.entity.QCommunityImage.communityImage;


@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Post> findAllPostWithNoOffset(String filter, Long lastValue, boolean isAsc, int limit, String keyword, Long categoryId) {
        QPost post = QPost.post;

        BooleanExpression byCategory = getCategory(categoryId, post);                   // 카테고리 구분
        BooleanExpression bySortFilter = getSortFilter(filter, lastValue, isAsc, post); // 정렬 필터
        BooleanExpression byKeyword = getKeyword(keyword, post);                        // 키워드 검색
        BooleanExpression notDeleted = getNotDeleted(post);                             // 삭제된글 제외
        OrderSpecifier<?> mainSort = getMainSort(filter, isAsc, post);                  // 첫번째 정렬 기준
        OrderSpecifier<?> subSort = getSubSort(isAsc, post);                            // 두번째 정렬 기준

        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.communityImages, communityImage)
                .leftJoin(communityImage.image, image)
                .where(notDeleted, byCategory, bySortFilter, byKeyword)
                .orderBy(mainSort, subSort)
                .limit(limit)
                .fetch();
    }

    /**
     * 삭제된 데이터는 제외
     */
    private BooleanExpression getNotDeleted(QPost post) {
        return post.deletedAt.isNull();
    }

    /**
     * 카테고리 구분
     */
    private BooleanExpression getCategory(Long categoryId, QPost post) {
        if (categoryId != null) {
            return post.category.id.eq(categoryId);
        }
        return null;
    }

    /**
     * 제목 + 내용으로 키워드 검색
     */
    private BooleanExpression getKeyword(String keyword, QPost post) {
        if (keyword != null) {
            return post.title.contains(keyword).or(post.content.contains(keyword));
        } else {
            return null;
        }
    }

    /**
     * 정렬 필터 선택
     * - isAsc -> true, 오름 차순
     */
    private BooleanExpression getSortFilter(String filter, Long lastValue, boolean isAsc, QPost post) {

        if (SortFilterType.LIKES.getValue().equalsIgnoreCase(filter) && lastValue != null) {
            return isAsc ? post.likes.gt(lastValue) : post.likes.lt(lastValue);

        } else if (SortFilterType.VIEWS.getValue().equalsIgnoreCase(filter) && lastValue != null) {
            return isAsc ? post.views.gt(lastValue) : post.views.lt(lastValue);

        } else if (lastValue != null) {
            return isAsc ? post.id.gt(lastValue) : post.id.lt(lastValue);

        }
        return null;
    }

    /**
     * 첫번째 정렬 기준
     */
    private OrderSpecifier<?> getMainSort(String filter, boolean isAsc, QPost post) {
        if (SortFilterType.LIKES.getValue().equalsIgnoreCase(filter)) {
            return isAsc ? post.likes.asc() : post.likes.desc();

        } else if (SortFilterType.VIEWS.getValue().equalsIgnoreCase(filter)) {
            return isAsc ? post.views.asc() : post.views.desc();

        }
        return isAsc ? post.id.asc() : post.id.desc();
    }

    /**
     * 두번째 정렬 기준
     */
    private OrderSpecifier<?> getSubSort(boolean isAsc, QPost post) {
        return isAsc ? post.id.asc() : post.id.desc();
    }
}
