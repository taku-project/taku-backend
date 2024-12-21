package com.ani.taku_backend.category.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import com.ani.taku_backend.category.service.CategoryService;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.global.response.ApiResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class RestCategoryController {

    private final CategoryService categoryService;
    

    @PostMapping("")
    @RequireUser
    public ApiResponse<ResponseCategoryDTO> createCategory(
        @RequestPart("category") RequestCategoryCreateDTO requestCategoryCreateDTO,
        @RequestPart("image") MultipartFile image,
        PrincipalUser principalUser
    ){
        ResponseCategoryDTO result = categoryService.createCategory(principalUser, requestCategoryCreateDTO, image);
        return ApiResponse.created(result);
    }

    @GetMapping("")
    public ApiResponse<Page<ResponseCategorySeachDTO>> searchCategories(
        @ModelAttribute RequestCategorySearch requestCategorySearch,
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        Page<ResponseCategorySeachDTO> result = categoryService.searchCategories(requestCategorySearch, pageable);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<ResponseCategoryDTO> findCategoryById(
        @PathVariable("id") Long id
    ) {
        ResponseCategoryDTO result = categoryService.findCategoryById(id);
        return ApiResponse.ok(result);
    }
    
}
