package com.ani.taku_backend.admin.category.service;

import com.ani.taku_backend.admin.category.dto.req.AdminCategoryCreateReqDTO;
import com.ani.taku_backend.admin.category.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.dto.req.UpdateCategoryReqDTO;
import com.ani.taku_backend.admin.category.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.user.model.entity.User;

public interface AdminCategoryService {
    AdminCategoryListResDTO findCategoryList(User user, AdminCategoryListReqDTO categoryListReqDTO);

    void createCategory(User user, AdminCategoryCreateReqDTO createReqDTO);

    void updateCategoryStatus(User user, UpdateCategoryReqDTO updateCategoryReqDTO);

    void deleteCategory(Long categoryId, User user);
}
