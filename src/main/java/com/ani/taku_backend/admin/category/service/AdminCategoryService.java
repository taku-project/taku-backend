package com.ani.taku_backend.admin.category.service;

import com.ani.taku_backend.admin.category.domain.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.req.UpdateCategoryReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.user.model.entity.User;

public interface AdminCategoryService {
    AdminCategoryListResDTO findCategoryList(User user, AdminCategoryListReqDTO categoryListReqDTO);

    void updateCategoryStatus(User user, UpdateCategoryReqDTO updateCategoryReqDTO);
}
