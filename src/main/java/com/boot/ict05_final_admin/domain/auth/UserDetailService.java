//package com.boot.ict05_final_admin.domain.auth;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//
//@Bean
//public UserDetailsService userDetailsService() {
//    return username -> userRepository.findByEmail(username)
//            .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
//}
