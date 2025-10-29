package com.boot.ict05_final_admin.domain.auth.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository repo;
    private final PasswordEncoder encoder;

    public boolean emailExists(String email) {
        return repo.existsByEmail(email.trim().toLowerCase());
    }

    public Long join(String email, String rawPw, String name, String phone) {
        String e = email.trim().toLowerCase();
        if (repo.existsByEmail(e)) throw new IllegalStateException("이미 존재하는 이메일");

        Member m = new Member();
        m.setEmail(e);
        m.setPassword(encoder.encode(rawPw));
        m.setName(name);
        m.setPhone(phone);
        return repo.save(m).getId();
    }
}