package com.ani.taku_backend.jangter.repository.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.QDuckuJangter;
import com.ani.taku_backend.jangter.model.entity.QItemCategories;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.Projections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DuckuJangterRepositoryCustomImpl implements DuckuJangterRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 추천 상품 1차 필터링 조회
     * @param keywords 키워드
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param itemCategoryId 카테고리 아이디
     * @param status 상태
     * @return 추천 상품 리스트
     */
    @Override
    public List<DuckuJangter> findRecommendFilteredProducts(List<String> keywords, BigDecimal minPrice,
            BigDecimal maxPrice, Long itemCategoryId, StatusType status, Long productId) {

        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;

        BooleanBuilder titleConditions = new BooleanBuilder();
        keywords.forEach(keyword -> 
            titleConditions.or(duckuJangter.title.containsIgnoreCase(keyword))
        );

        List<DuckuJangter> fetch = this.jpaQueryFactory.selectFrom(duckuJangter)
            .where(
                titleConditions,
                duckuJangter.price.between(minPrice, maxPrice),
                duckuJangter.itemCategories.id.eq(itemCategoryId),
                duckuJangter.status.eq(status),
                duckuJangter.id.ne(productId)
            )
            .distinct()
            .fetch();

        log.info("fetch : {}", fetch);

        return fetch;
    }

    @Override
    public List<CategoryGroupCountDTO> findCategoryGroupCount() {
        QDuckuJangter duckuJangter = QDuckuJangter.duckuJangter;
        QItemCategories itemCategories = QItemCategories.itemCategories;

        return jpaQueryFactory
            .select(Projections.constructor(CategoryGroupCountDTO.class,
                itemCategories.id,
                itemCategories.name,
                duckuJangter.count()))
            .from(duckuJangter)
            .leftJoin(duckuJangter.itemCategories, itemCategories)
            .where(duckuJangter.deletedAt.isNull())
            .groupBy(itemCategories.id, itemCategories.name)
            .orderBy(duckuJangter.count().desc())
            .fetch();
    }
}
