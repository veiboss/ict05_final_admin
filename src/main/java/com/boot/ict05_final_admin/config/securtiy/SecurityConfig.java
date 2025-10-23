package com.boot.ict05_final_admin.config.securtiy;

import com.boot.ict05_final_admin.domain.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService());
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF ON (폼에 _csrf 히든필드 필요)
                .csrf(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                // 루트는 컨트롤러에서 /login으로 리다이렉트
                                "/login", "/join", "/join-submit",
                                "/css/**", "/js/**", "/images/**",
                                "/favicon.ico", "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/login")              // GET 화면
                        .loginProcessingUrl("/login")     // POST 처리
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true) // ✅ 내부 첫화면으로
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")               // 기본 POST
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .authenticationProvider(authProvider());

        return http.build();
    }
}
