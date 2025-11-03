package com.boot.ict05_final_admin.config.security;

import com.boot.ict05_final_admin.domain.auth.service.MemberUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final MemberUserDetailsService memberUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // DB는 $2a$... 해시만 (접두어 {bcrypt} 금지)
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(memberUserDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Thymeleaf 폼에 CSRF 히든 필드 있다면 활성 유지
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/assets/**", "/api/auth/**", "/uploads/**").permitAll()
                        // 필요시 .requestMatchers("/admin/**").hasRole("HQ")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")                 // GET
                        .loginProcessingUrl("/login")        // POST: 시큐리티가 처리
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )
                .authenticationProvider(daoAuthProvider());

        return http.build();
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 로컬 테스트용 업로드 폴더 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///D:/ict05_uploads/");
    }

}
