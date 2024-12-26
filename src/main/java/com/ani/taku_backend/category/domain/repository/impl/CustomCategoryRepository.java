package com.ani.taku_backend.category.domain.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;

public interface CustomCategoryRepository {

    Page<ResponseCategorySeachDTO> searchCategories(RequestCategorySearch condition, Pageable pageable);
}
