package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService {
    ResponseCategoryDTO createCategory(PrincipalUser principalUser, RequestCategoryCreateDTO requestCategoryCreateDTO, MultipartFile uploadFile);

    Page<ResponseCategorySeachDTO> searchCategories(RequestCategorySearch requestCategorySearch, Pageable pageable);

    ResponseCategoryDTO findCategoryById(Long id);
}
