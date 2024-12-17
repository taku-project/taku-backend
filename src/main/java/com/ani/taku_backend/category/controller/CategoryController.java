package com.ani.taku_backend.category.controller;

import com.ani.taku_backend.category.domain.dto.AniCategoryListDTO;
import com.ani.taku_backend.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/")
    public String findCategoryAll(Model model) {
        model.addAttribute("categories", new AniCategoryListDTO(1L));
        return "category/list";
    }
}
