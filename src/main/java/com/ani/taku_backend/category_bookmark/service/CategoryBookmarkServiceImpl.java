package com.ani.taku_backend.category_bookmark.service;

import com.ani.taku_backend.category_bookmark.domain.CategoryBookmark;
import com.ani.taku_backend.category_bookmark.domain.repository.CategoryBookmarkRepository;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.category_bookmark.dto.res.CategoryBookmarkDTO;
import com.ani.taku_backend.category_bookmark.dto.res.CategoryBookmarkReqDTO;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryBookmarkServiceImpl implements CategoryBookmarkService {
    private final CategoryBookmarkRepository categoryBookmarkRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createCategoryBookmark(User user, Long categoryId) {
        Optional<CategoryBookmark> userCategory = categoryBookmarkRepository.findByCategoryIdAndUserUserId(categoryId, user.getUserId());

        if(userCategory.isEmpty()) {
            Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY));
            CategoryBookmark categoryBookmark = CategoryBookmark.create(user, category);

            categoryBookmarkRepository.save(categoryBookmark);
        }
    }

    @Override
    public void deleteCategoryBookmark(User user, Long categoryId) {
        categoryBookmarkRepository
            .findByCategoryIdAndUserUserId(categoryId, user.getUserId())
            .ifPresent(categoryBookmarkRepository::delete);
    }

    @Override
    public CategoryBookmarkReqDTO findCategoryBookmark(User user) {
        List<CategoryBookmarkDTO> userCategoryBookmark = categoryBookmarkRepository.findByUserUserId(user.getUserId());

        return CategoryBookmarkReqDTO.builder()
                .categoryBookmarks(userCategoryBookmark)
                .build();
    }

}
