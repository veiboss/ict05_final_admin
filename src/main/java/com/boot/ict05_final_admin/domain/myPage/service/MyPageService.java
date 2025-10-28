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
}
