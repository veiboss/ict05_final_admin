package com.boot.ict05_final_admin.domain.auth;

import com.boot.ict05_final_admin.domain.auth.UserService.JoinForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // GET /login -> templates/login/login.html
    @GetMapping("/login")
    public String loginPage() {
        return "login/login";
    }

    // GET /join -> templates/login/join.html
    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("form", new JoinForm());
        return "login/join";
    }

    // POST /join-submit
    @PostMapping("/join-submit")
    public String joinSubmit(@Valid @ModelAttribute("form") JoinForm form,
                             BindingResult binding,
                             Model model) {
        if (binding.hasErrors()) return "login/join";
        try {
            userService.register(form);
            return "redirect:/login?joined";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("dupError", ex.getMessage());
            return "login/join";
        }
    }
}
