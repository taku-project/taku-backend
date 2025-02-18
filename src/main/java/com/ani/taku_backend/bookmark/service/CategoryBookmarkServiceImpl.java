package com.ani.taku_backend.bookmark.service;

import com.ani.taku_backend.bookmark.domain.CategoryBookmark;
import com.ani.taku_backend.bookmark.domain.repository.CategoryBookmarkRepository;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryBookmarkServiceImpl implements CategoryBookmarkService {
    private final CategoryBookmarkRepository categoryBookmarkRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createCategoryBookmark(User user, Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY));
        CategoryBookmark categoryBookmark = CategoryBookmark.create(user, category);

        categoryBookmarkRepository.save(categoryBookmark);
    }

    @Override
    public void deleteCategoryBookmark(User user, Long categoryId) {
        CategoryBookmark categoryBookmark = categoryBookmarkRepository
                .findByCategoryIdAndUserUserId(categoryId, user.getUserId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY_BOOKMARK));

        categoryBookmarkRepository.delete(categoryBookmark);
    }

}
