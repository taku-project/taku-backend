package com.ani.taku_backend.admin.category.controller;

import com.ani.taku_backend.admin.category.domain.dto.req.AdminCategoryCreateReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.req.UpdateCategoryReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.admin.category.service.AdminCategoryService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/category")
@Controller
@Validated
@RequiredArgsConstructor
public class AdminCategoryController {
    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public String findCategoryList(@AuthenticationPrincipal PrincipalUser principalUser,
        AdminCategoryListReqDTO categoryListReqDTO, Model model,Pageable pageable) {

        User user = principalUser.getUser();
        categoryListReqDTO.setPageable(pageable);
        AdminCategoryListResDTO categoryList = adminCategoryService.findCategoryList(user, categoryListReqDTO);
        //문자열 전송
        model.addAttribute("d1", "서버로부터 <b>전송</b>");
        String str = "결과 메시지";
        int num = 300;
        model.addAttribute("d2", str);
        model.addAttribute("d3", num);

        return "category/list";
    }

    @PutMapping("/status")
    public String updateCategoryStatus(@AuthenticationPrincipal PrincipalUser principalUser,
        @Valid @RequestBody UpdateCategoryReqDTO updateCategoryReqDTO) {
        User user = principalUser.getUser();
        adminCategoryService.updateCategoryStatus(user, updateCategoryReqDTO);

        return "category/list";
    }


    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String createCategory(@AuthenticationPrincipal PrincipalUser principalUser,
        @ModelAttribute AdminCategoryCreateReqDTO createReqDTO) {
        User user = principalUser.getUser();

        adminCategoryService.createCategory(user, createReqDTO);

        return "category/list";
    }
}
