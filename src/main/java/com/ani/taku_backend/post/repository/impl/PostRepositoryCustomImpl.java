package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.SortFilterType;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.entity.QPost;
import com.ani.taku_backend.post.repository.impl.dto.FindAllPostQuerydslDTO;
import com.ani.taku_backend.post.repository.impl.dto.QFindAllPostQuerydslDTO;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ani.taku_backend.common.model.entity.QImage.image;
import static com.ani.taku_backend.post.model.entity.QCommunityImage.communityImage;
import static com.ani.taku_backend.post.model.entity.QPost.post;


@Repository
@RequiredArgsConstructor
@Slf4j
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FindAllPostQuerydslDTO> findAllPostWithNoOffset(PostListRequestDTO postListRequestDTO) {

        // DTO 값 꺼내기
        String sortFilterType = postListRequestDTO.getSortFilterType();
        long lastValue = postListRequestDTO.getLastValue();
        int limit = postListRequestDTO.getLimit();
        String keyword = postListRequestDTO.getKeyword();
        boolean asc = postListRequestDTO.isAsc();
        long categoryId = postListRequestDTO.getCategoryId();

        BooleanExpression byCategory = getCategory(categoryId, post);                           // 카테고리 구분
        BooleanExpression bySortFilter = getSortFilter(sortFilterType, lastValue, asc, post);   // 정렬 필터
        BooleanExpression byKeyword = getKeyword(keyword, post);                                // 키워드 검색
        BooleanExpression notDeleted = getNotDeleted(post);                                     // 삭제된글 제외
        OrderSpecifier<?> mainSort = getMainSort(sortFilterType, asc, post);                    // 첫번째 정렬 기준
        OrderSpecifier<?> subSort = getSubSort(asc, post);                                      // 두번째 정렬 기준

        return jpaQueryFactory
                .select(new QFindAllPostQuerydslDTO(
                        post.id,
                        post.user.userId,
                        post.category.id,
                        post.title,
                        post.content,
                        communityImage.image.imageUrl,
                        post.updatedAt,
                        post.views
                ))
                .from(post)
                .leftJoin(post.communityImages, communityImage)
                .leftJoin(communityImage.image, image)
                .where(notDeleted, byCategory, bySortFilter, byKeyword)
                .groupBy(post.id)
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
        if (keyword != null && !keyword.isEmpty()) {
            return post.title.contains(keyword).or(post.content.contains(keyword));
        } else {
            return null;
        }
    }

    /**
     * 정렬 필터 선택
     * - isAsc -> true, 오름 차순
     */
    private BooleanExpression getSortFilter(String sortFilterType, Long lastValue, boolean isAsc, QPost post) {

        if (sortFilterType.equals(SortFilterType.LIKES.getValue()) && lastValue != null && lastValue > 0) {
//            return isAsc ? post.likes.gt(lastValue) : post.likes.lt(lastValue);
            return null;        // 좋아요 기능 -> 몽고DB사용, 좋아요 개발 되면 붙이기

        } else if (sortFilterType.equals(SortFilterType.VIEWS.getValue()) && lastValue != null && lastValue > 0) {
            return isAsc ? post.views.gt(lastValue) : post.views.lt(lastValue);

        } else if (lastValue != null && lastValue > 0) {
            return isAsc ? post.id.gt(lastValue) : post.id.lt(lastValue);

        }
        return null;
    }

    /**
     * 첫번째 정렬 기준
     */
    private OrderSpecifier<?> getMainSort(String sortFilterType, boolean isAsc, QPost post) {
        if (sortFilterType.equals(SortFilterType.LIKES.getValue())) {
//            return isAsc ? post.likes.asc() : post.likes.desc();
            return null;        // 좋아요 기능 -> 몽고DB사용, 좋아요 개발 되면 붙이기

        } else if (sortFilterType.equals(SortFilterType.VIEWS.getValue())) {
            return isAsc ? post.views.asc() : post.views.desc();

        }
        return isAsc ? post.id.asc() : post.id.desc();
    }

    /**
     * 두번째 정렬 기준 - 무조건 id 순서
     */
    private OrderSpecifier<?> getSubSort(boolean isAsc, QPost post) {
        return isAsc ? post.id.asc() : post.id.desc();
    }
}
