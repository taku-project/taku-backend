package com.ani.taku_backend.category.domain.repository.impl;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;

import java.util.Optional;

public interface CustomCategoryRepository {

    Page<ResponseCategorySeachDTO> searchCategories(RequestCategorySearch condition, Pageable pageable);

    Optional<Category> findCategoryById(Long id, User user);
}
