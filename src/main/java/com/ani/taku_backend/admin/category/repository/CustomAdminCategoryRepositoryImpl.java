package com.ani.taku_backend.admin.category.repository;

import com.ani.taku_backend.admin.category.dto.CategorySearchType;
import com.ani.taku_backend.admin.category.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryOrderType;
import com.ani.taku_backend.category.domain.entity.CategoryStatus;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.category.domain.entity.QAnimationGenre.animationGenre;
import static com.ani.taku_backend.category.domain.entity.QCategory.category;
import static com.ani.taku_backend.category.domain.entity.QCategoryGenre.categoryGenre;
import static com.ani.taku_backend.category.domain.entity.QCategoryImage.categoryImage;

@Repository
@RequiredArgsConstructor
public class CustomAdminCategoryRepositoryImpl implements CustomAdminCategoryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Category> findCategoryList(Long userId, AdminCategoryListReqDTO categoryListReqDTO) {
        Pageable pageable = categoryListReqDTO.getPageable();

        List<Long> categoryIds = queryFactory
                .select(category.id)
                .from(category)
                .where(
                    keywordCondition(categoryListReqDTO.getKeyword(), categoryListReqDTO.getSearchType()),
                    statusCondition(categoryListReqDTO.getStatus())
                )
                .orderBy(orderCondition(pageable.getSort()))
                .limit(pageable.getPageSize())
                .fetch();

        List<Category> categoryList = queryFactory
                .selectFrom(category)
                .leftJoin(category.categoryImage, categoryImage).fetchJoin()
                .leftJoin(category.categoryGenres, categoryGenre).fetchJoin()
                .leftJoin(categoryGenre.genre, animationGenre).fetchJoin()
                .where(category.id.in(categoryIds))
                .orderBy(orderCondition(pageable.getSort()))
                .fetch();

        return new PageImpl<>(categoryList, pageable, categoryIds.size());
    }

    private BooleanExpression statusCondition(CategoryStatus status) {
        if(status == null) {
            return null;
        }
        return category.status.eq(status);
    }

    private BooleanExpression keywordCondition(String keyword, CategorySearchType searchType) {
        if(StringUtils.hasText(keyword)) {
            if(CategorySearchType.ID.name().equals(searchType.name())) {
                return category.id.eq(Long.parseLong(keyword));
            } else if(CategorySearchType.USERNAME.name().equals(searchType.name())) {
                return category.user.nickname.eq(keyword);
            } else if(CategorySearchType.CATEGORY_NAME.name().equals(searchType.name())) {
                return category.name.eq(keyword);
            }
        }
        return null;
    }

    private OrderSpecifier[] orderCondition(Sort sort) {
        List<OrderSpecifier> ORDERS = new ArrayList<>();

        if (sort.isEmpty()) {
            ORDERS.add(new OrderSpecifier(Order.DESC, category.id));
        } else {
            for (Sort.Order order : sort) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

                if(CategoryOrderType.ID.name().equals(order.getProperty())) {
                    ORDERS.add(new OrderSpecifier(direction, category.id));
                } else if(CategoryOrderType.NAME.name().equals(order.getProperty())) {
                    ORDERS.add(new OrderSpecifier(direction, category.name));
                } else if(CategoryOrderType.CREATED_AT.name().equals(order.getProperty())) {
                    ORDERS.add(new OrderSpecifier(direction, category.createdType));
                } else if(CategoryOrderType.MODIFIED_AT.name().equals(order.getProperty())) {
                    ORDERS.add(new OrderSpecifier(direction, category.status));
                }
            }
        }

        return ORDERS.toArray(OrderSpecifier[]::new);
    }
}
