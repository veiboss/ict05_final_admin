package com.boot.ict05_final_admin.domain.myPage.repository;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyPageRepository extends JpaRepository<Member, Long>{

    // 이메일로 회원 조회 (로그인 후 마이페이지용)
    Optional<Member> findByEmail(String email);
}
