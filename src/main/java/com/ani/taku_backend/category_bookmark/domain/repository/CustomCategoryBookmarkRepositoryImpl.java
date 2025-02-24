package com.ani.taku_backend.category_bookmark.domain.repository;

import com.ani.taku_backend.category_bookmark.dto.res.CategoryBookmarkDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.ani.taku_backend.category.domain.entity.QCategory.category;
import static com.ani.taku_backend.category.domain.entity.QCategoryImage.categoryImage;
import static com.ani.taku_backend.category_bookmark.domain.QCategoryBookmark.categoryBookmark;
import static com.ani.taku_backend.common.model.entity.QImage.image;

@RequiredArgsConstructor
public class CustomCategoryBookmarkRepositoryImpl implements CustomCategoryBookmarkRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CategoryBookmarkDTO> findByUserUserId(Long userId) {
        return queryFactory.select(
                    Projections.constructor(CategoryBookmarkDTO.class,
                        categoryBookmark.id,
                        category.id,
                        category.name,
                        image.imageUrl
                    )
                )
                .from(categoryBookmark)
                .leftJoin(categoryBookmark.category, category)
                .leftJoin(category.categoryImage, categoryImage)
                .leftJoin(categoryImage.image, image)
                .where(categoryBookmark.user.userId.eq(userId))
                .orderBy(categoryBookmark.id.desc())
                .fetch();
    }
}
