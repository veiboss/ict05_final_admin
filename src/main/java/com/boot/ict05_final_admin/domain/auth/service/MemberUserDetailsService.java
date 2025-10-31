// src/main/java/com/boot/ict05_final_admin/domain/auth/service/MemberUserDetailsService.java
package com.boot.ict05_final_admin.domain.auth.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member m = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("no user: " + email));

        // DB 비번은 passwordEncoder.encode(...)로 저장되어 있어야 함.  {bcrypt}... 형태면 OK
        return User.withUsername(m.getEmail())
                .password(m.getPassword())
                .roles("HQ") // 권한은 임시로 HQ
                .build();
    }
}
