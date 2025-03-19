package com.ani.taku_backend.category.domain.repository.impl;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ani.taku_backend.category.dto.CategorySearchReqDTO;
import com.ani.taku_backend.category.dto.CategorySeachResDTO;

import java.util.Optional;

public interface CustomCategoryRepository {

    Page<CategorySeachResDTO> searchCategories(CategorySearchReqDTO condition, Pageable pageable);

    Optional<Category> findCategoryById(Long id, User user);
}
