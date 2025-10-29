package com.boot.ict05_final_admin.domain.myPage.repository;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyPageRepository extends JpaRepository<Member, Long>{

    // 이메일로 회원 조회 (로그인 후 마이페이지용)
    Optional<Member> findByEmail(String email);

    // 활성 회원만 조회 (탈퇴 제외)
    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE'")
    List<Member> findActiveMembers();
}
