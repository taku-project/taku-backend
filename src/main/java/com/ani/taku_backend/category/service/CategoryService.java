package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.dto.AniGenreListReqDTO;
import com.ani.taku_backend.category.dto.CreateCategoryReqDTO;
import com.ani.taku_backend.category.dto.CategorySearchReqDTO;
import com.ani.taku_backend.category.dto.CategoryResDTO;
import com.ani.taku_backend.category.dto.CategorySeachResDTO;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResDTO createCategory(User principalUser, CreateCategoryReqDTO createCategoryReqDTO);

    Page<CategorySeachResDTO> searchCategories(CategorySearchReqDTO categorySearchReqDTO, Pageable pageable);

    CategoryResDTO findCategoryById(Long id, User user);

    AniGenreListReqDTO findAniGenres(String keyword);
}
