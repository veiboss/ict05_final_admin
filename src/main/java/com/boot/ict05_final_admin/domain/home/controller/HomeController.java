package com.boot.ict05_final_admin.domain.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "login";  // 진입 시 로그인으로
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "index"; // 관리자 첫 화면 템플릿
    }
}
