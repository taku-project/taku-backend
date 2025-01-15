package com.ani.taku_backend.admin.category.controller;

import com.ani.taku_backend.admin.category.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.admin.category.service.AdminCategoryService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/category")
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
}
