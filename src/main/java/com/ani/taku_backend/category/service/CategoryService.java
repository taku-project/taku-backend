package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.domain.dto.AniGenreListReqDTO;
import com.ani.taku_backend.category.domain.dto.CategoryCreateReqDTO;
import com.ani.taku_backend.category.domain.dto.CategorySearchReqDTO;
import com.ani.taku_backend.category.domain.dto.CategoryResDTO;
import com.ani.taku_backend.category.domain.dto.CategorySeachResDTO;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResDTO createCategory(User principalUser, CategoryCreateReqDTO categoryCreateReqDTO);

    Page<CategorySeachResDTO> searchCategories(CategorySearchReqDTO categorySearchReqDTO, Pageable pageable);

    CategoryResDTO findCategoryById(Long id, User user);

    AniGenreListReqDTO findAniGenres(String keyword);
}
