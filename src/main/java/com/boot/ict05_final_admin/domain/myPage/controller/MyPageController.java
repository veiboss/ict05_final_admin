package com.boot.ict05_final_admin.domain.myPage.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.myPage.dto.MyPageDTO;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 조회
     * - 로그인 전에는 임시 memberId로 테스트
     * - 로그인 연동 시 SecurityContext에서 memberId 자동 추출
     */
    @GetMapping("/mypage")
    public String myPage(Model model) {

        // ===== 로그인 연동 이후 버전 =====
        //Long memberId = getLoginMemberId();

        // ===== 로그인 전 임시 버전 =====
        Long memberId = 1L;

        // 마이페이지 조회
        MyPageDTO dto = myPageService.getMyPage(memberId);
        model.addAttribute("member", dto);
        return "mypage/view";
    }

    /**
     * SecurityContextHolder에서 로그인된 회원 ID 가져오기
     * - 로그인 기능 연동되면 자동 활성화
     */
//    private Long getLoginMemberId() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            // 인증되지 않은 경우 null 리턴
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return null;
//            }
//
//            Object principal = authentication.getPrincipal();
//
//            // principal이 Member 타입일 경우 (UserDetails 직접 반환한 구조)
//            if (principal instanceof Member member) {
//                return member.getId();
//            }
//
//            // principal이 UserDetails 구현체일 경우 (username = email)
//            if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
//                // 이메일(username)로 Member 조회
//                Member member = myPageService.findByEmail(userDetails.getUsername());
//                return member.getId();
//            }
//
//            return null;
//        } catch (Exception e) {
//            return null;
//        }
//    }

}
