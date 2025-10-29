package com.boot.ict05_final_admin.config.securtiy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.formLogin(f -> f.disable());
        http.logout(l -> l.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login","/register","/assets/**","/css/**","/js/**","/images/**").permitAll()
                .anyRequest().authenticated()
        );
        return http.build();
    }
}
