package com.boot.ict05_final_admin.config.web;

import com.boot.ict05_final_admin.config.interceptor.LoginSessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginSessionInterceptor())
                .addPathPatterns("/**")
                // 정적 리소스 & 인증 페이지는 제외
                .excludePathPatterns(
                        "/css/**", "/js/**", "/images/**",
                        "/login", "/join", "/join-submit"
                );
    }

    // 정적리소스 캐시 등 필요시
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기본 static 사용이면 생략 가능
    }
}
