package com.ani.taku_backend.admin.category.repository;

import com.ani.taku_backend.admin.category.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import org.springframework.data.domain.Page;

public interface CustomAdminCategoryRepository {
    Page<Category> findCategoryList(Long userId, AdminCategoryListReqDTO categoryListReqDTO);
}
