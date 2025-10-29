package com.boot.ict05_final_admin.web.auth;

import com.boot.ict05_final_admin.common.util.CookieUtil;
import com.boot.ict05_final_admin.config.HqProps;
import com.boot.ict05_final_admin.domain.auth.dto.LoginRequest;
import com.boot.ict05_final_admin.domain.auth.service.AuthGateway;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class AuthController {
    private final AuthGateway gateway;
    private final HqProps props;

    @GetMapping("/login")
    public String loginPage() { return "auth/login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpServletResponse res, RedirectAttributes ra) {
        try {
            var token = gateway.login(new LoginRequest(email, password));
            CookieUtil.write(res,"accessToken",token.accessToken(),props.getCookieDomain(),props.isCookieSecure(),3600);
            CookieUtil.write(res,"refreshToken",token.refreshToken(),props.getCookieDomain(),props.isCookieSecure(),60*60*24*14);
            return "redirect:/"; // /admin/
        } catch (Exception e) {
            ra.addFlashAttribute("loginError","이메일 또는 비밀번호를 확인해 주세요.");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "auth/register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String name,
                             @RequestParam(required=false) String phone,
                             RedirectAttributes ra) {
        gateway.join(Map.of("email",email,"password",password,"name",name,"phone",phone));
        ra.addFlashAttribute("msg","회원가입이 완료되었습니다. 로그인해 주세요.");
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse res) {
        CookieUtil.clear(res,"accessToken",props.getCookieDomain(),props.isCookieSecure());
        CookieUtil.clear(res,"refreshToken",props.getCookieDomain(),props.isCookieSecure());
        return "redirect:/login";
    }
}
