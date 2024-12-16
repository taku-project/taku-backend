package com.ani.taku_backend.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 1. 클래스명 : AdminController
 * 2. 설명 : 관리자 페이지 컨트롤러
 * 3. 작성일 : 2024-12-10
 * 4. 작성자 : KORMAP
 */

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminMain(Model model) {
        //문자열 전송
        model.addAttribute("d1", "서버로부터 <b>전송</b>");
        String str = "결과 메시지";
        int num = 300;
        model.addAttribute("d2", str);
        model.addAttribute("d3", num);

        return "index";
    }

    @GetMapping("forbiddenList")
    public String forbiddenList(Model model) {
        model.addAttribute("data", "전달된 데이터 값");
        return "forbiddenList";
    }

}
