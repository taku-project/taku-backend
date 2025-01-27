package com.ani.taku_backend.admin.category.service;

import com.ani.taku_backend.admin.category.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Pageable;

public interface AdminCategoryService {
    AdminCategoryListResDTO findCategoryList(User user, AdminCategoryListReqDTO categoryListReqDTO);
}
