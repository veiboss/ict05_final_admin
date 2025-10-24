//package com.boot.ict05_final_admin.domain.auth;
//
//
//import jakarta.transaction.Transactional;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Getter
//    @Setter
//    public static class JoinForm {
//        @NotBlank
//        private String name;
//        @Email
//        @NotBlank private String email;
//        @NotBlank private String password;
//        private String phone; // 선택
//    }
//
//    @Transactional
//    public Long register(JoinForm form) {
//        if (userRepository.existsByEmail(form.getEmail())) {
//            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
//        }
//        if (form.getPhone() != null && !form.getPhone().isBlank()
//                && userRepository.existsByPhone(form.getPhone())) {
//            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
//        }
//
//        User u = User.builder()
//                .name(form.getName())
//                .email(form.getEmail())
//                .phone(form.getPhone())
//                .password(passwordEncoder.encode(form.getPassword()))
//                .build(); // role은 Transient(기본 ROLE_HQ)
//        return userRepository.save(u).getId();
//    }
//}
