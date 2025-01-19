package com.ani.taku_backend.user_jangter.repository;

import com.ani.taku_backend.user_jangter.domain.CompleteJangterSortType;
import com.ani.taku_backend.user_jangter.dto.QUserPurchaseResponseDTO;
import com.ani.taku_backend.user_jangter.dto.UserPurchaseResponseDTO;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.marketprice.model.entity.QCompletedDeal.completedDeal;

@Repository
@RequiredArgsConstructor
public class UserJangterRepositoryImpl implements UserJangterRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public PageImpl<UserPurchaseResponseDTO> findUserPurchaseList(Long userId, Pageable pageable) {
        // TODO 검색 키워드 있으면 리팩토링 하기
        long totalCount = queryFactory
                .select(completedDeal.count())
                .from(completedDeal)
                .where(
                    eqPurchaseId(userId)
                )
                .fetchOne();

        List<UserPurchaseResponseDTO> userPurchaseResponseDTOList =
                queryFactory
                .select(
                    new QUserPurchaseResponseDTO(
                        completedDeal.id,
                        completedDeal.product.id,
                        completedDeal.title,
                        completedDeal.price,
                        completedDeal.categoryName
                    )
                )
                .where(
                    completedDeal.purchaseUserId.eq(userId)
                )
                .from(completedDeal)
                .orderBy(orderCondition(pageable.getSort()))
                .limit(pageable.getPageSize())
                .fetch();
        return new PageImpl<>(userPurchaseResponseDTOList, pageable, totalCount);
    }

    private BooleanExpression eqPurchaseId(Long purchaseId) {
        return completedDeal.purchaseUserId.eq(purchaseId);
    }
    private OrderSpecifier[] orderCondition(Sort sort) {
        List<OrderSpecifier> ORDERS = new ArrayList<>();

        if (sort.isEmpty()) {
            ORDERS.add(new OrderSpecifier(Order.DESC, completedDeal.id));
        } else {
            for (Sort.Order order : sort) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

                CompleteJangterSortType sortType = CompleteJangterSortType.valueOf(order.getProperty().toUpperCase());

                switch (sortType) {
                    case ID : ORDERS.add(new OrderSpecifier(direction, completedDeal.id)); break;
                    case TITLE : ORDERS.add(new OrderSpecifier(direction, completedDeal.title)); break;
                    case CATEGORY_NAME : ORDERS.add(new OrderSpecifier(direction, completedDeal.categoryName)); break;
                    case PRICE : ORDERS.add(new OrderSpecifier(direction, completedDeal.price)); break;
                }
            }
        }

        return ORDERS.toArray(OrderSpecifier[]::new);
    }
}