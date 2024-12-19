package com.ani.taku_backend.category.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.service.CategoryService;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.model.dto.CreateImageDTO;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.FileUtil;
import com.ani.taku_backend.global.exception.CustomException;
import com.ani.taku_backend.global.exception.ErrorCode;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class RestCategoryController {

    private final CategoryService categoryService;
    private final FileService fileService;
    

    @PostMapping("")
    @RequireUser
    public ResponseCategoryDTO createCategory(
        @RequestPart("category") RequestCategoryCreateDTO requestCategoryCreateDTO,
        @RequestPart("image") MultipartFile image,
        PrincipalUser principalUser
    ){
        return categoryService.createCategory(principalUser, requestCategoryCreateDTO, image);
    }
    
}
