package com.boot.ict05_final_admin.domain.myPage.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.myPage.dto.MyPageDTO;
import com.boot.ict05_final_admin.domain.myPage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class MyPageService {

    private final MyPageRepository myPageRepository;

    @Transactional(readOnly = true)
    public MyPageDTO getMyPage(Long memberId) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return MyPageDTO.fromEntity(member);
    }

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return myPageRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원이 존재하지 않습니다."));
    }

    // 정보 수정
    @Transactional
    public void updateMember(MyPageDTO dto) {
        Member member = myPageRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // DTO 값으로 업데이트
        member.updateProfile(dto.getName(), dto.getPhone());
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 일치 여부 확인 후 암호화 저장
     */
    @Transactional
    public void updatePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

//        TODO : Security 활성화 되면
//        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
//            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
//        }
//        // 새 비밀번호 암호화 후 저장
//        member.setPassword(passwordEncoder.encode(newPassword));

        // Security 비활성화 임시 대응
        if (!member.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 임시로 암호화 없이 그대로 저장 (테스트용)
        member.setPassword(newPassword);
    }

    // 비밀번호 검증
    @Transactional(readOnly = true)
    public boolean checkCurrentPassword(Long memberId, String currentPassword) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // return passwordEncoder.matches(currentPassword, member.getPassword());
        return member.getPassword().equals(currentPassword);
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    public void withdrawMember(Long memberId) {
        System.out.println(">>> 탈퇴 서비스 진입 확인");
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        member.withdraw(); // 상태를 WITHDRAWN으로 변경
    }

}
