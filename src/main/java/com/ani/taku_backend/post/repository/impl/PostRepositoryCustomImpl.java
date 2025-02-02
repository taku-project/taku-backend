package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.SortFilterType;
import com.ani.taku_backend.post.model.dto.FindPostQueryDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.dto.QFindPostQueryDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
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
    public Page<FindPostQueryDTO> findPostListPage(PostListRequestDTO postListRequestDTO, Pageable pageable) {
        String keyword = postListRequestDTO.getKeyword();
        Long categoryId = postListRequestDTO.getCategoryId();

        BooleanBuilder predicate = getBooleanBuilder(categoryId, keyword);

        OrderSpecifier<Long> orderSpecifier = getOrderSpecifier(pageable);

        List<FindPostQueryDTO> results = jpaQueryFactory
                .select(new QFindPostQueryDTO(
                        post.id,
                        post.user.userId,
                        post.category.id,
                        post.title,
                        post.content,
                        image.imageUrl,
                        post.updatedAt,
                        post.views,
                        post.user.nickname,
                        post.user.profileImg
                ))
                .from(post)
                .leftJoin(post.communityImages, communityImage)
                .leftJoin(communityImage.image, image)
                .where(predicate)
                .orderBy(orderSpecifier)
                .groupBy(post.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long countActivePostsByCategory = getCountActivePostsByCategory(categoryId);

        return new PageImpl<>(results, pageable, countActivePostsByCategory);
    }

    private BooleanBuilder getBooleanBuilder(long categoryId, String keyword) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(getNotDeleted());
        predicate.and(getCategory(categoryId));
        predicate.and(getKeyword(keyword));
        return predicate;
    }

    private Long getCountActivePostsByCategory(Long categoryId) {
        return jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(getNotDeleted().and(getCategory(categoryId)))
                .fetchOne();
    }

    /**
     * 삭제된 데이터는 제외
     */
    private BooleanExpression getNotDeleted() {
        return post.deletedAt.isNull();
    }

    /**
     * 카테고리 구분
     */
    private BooleanExpression getCategory(Long categoryId) {
        if (categoryId != null) {
            return post.category.id.eq(categoryId);
        }
        return null;
    }

    /**
     * 제목 + 내용으로 키워드 검색
     */
    private BooleanExpression getKeyword(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return post.title.contains(keyword).or(post.content.contains(keyword));
        } else {
            return null;
        }
    }

    /**
     * 정렬 필터를 기반으로 정렬 적용
     */
    private OrderSpecifier<Long> getOrderSpecifier(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return post.id.desc();
        }

        SortFilterType sortFilter = SortFilterType.ID;
        boolean isAscending = false;

        for (Order order : pageable.getSort()) {
            sortFilter = SortFilterType.valueOf(order.getProperty().toUpperCase());
            isAscending = order.isAscending();
        }

        // 필터 기준에 맞춰 OrderSpecifier 반환
        return switch (sortFilter) {
            case ID -> isAscending ? post.id.asc() : post.id.desc();
            case VIEWS -> isAscending ? post.views.asc() : post.views.desc();
            default -> post.id.desc();
        };
    }

}


