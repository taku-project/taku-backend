package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.domain.dto.AniGenreListReqDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    ResponseCategoryDTO createCategory(PrincipalUser principalUser, RequestCategoryCreateDTO requestCategoryCreateDTO);

    Page<ResponseCategorySeachDTO> searchCategories(RequestCategorySearch requestCategorySearch, Pageable pageable);

    ResponseCategoryDTO findCategoryById(Long id);

    AniGenreListReqDTO findAniGenres(String keyword);
}
