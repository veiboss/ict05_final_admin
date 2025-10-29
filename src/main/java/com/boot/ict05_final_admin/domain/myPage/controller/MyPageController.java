package com.boot.ict05_final_admin.domain.myPage.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.myPage.dto.MyPageDTO;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        Long memberId = 52L;

        // 마이페이지 조회
        MyPageDTO dto = myPageService.getMyPage(memberId);
        model.addAttribute("member", dto);
        return "mypage/view";
    }

    /**
     * SecurityContextHolder에서 로그인된 회원 ID 가져오기
     * - 로그인 기능 연동되면 자동 활성화
     */
//    TODO : 로그인 활성화 되면
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

    /**
     * 회원 정보 및 비밀번호 수정 폼 페이지
     * - 기존 회원 데이터를 불러와 수정 입력폼에 표시
     */
    @GetMapping("/mypage/modify")
    public String modifyForm(Model model) {

        // Long memberId = ((Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        Long memberId = 52L; // 로그인 연동 전 임시 ID
        MyPageDTO dto = myPageService.getMyPage(memberId);
        model.addAttribute("member", dto);
        return "mypage/modify";
    }

    /**
     * 회원 정보 및 비밀번호 수정 처리
     * - 이름·전화번호·비밀번호를 한 번에 처리
     * - 비밀번호 입력칸이 비어 있으면 이름/전화번호만 수정
     */
    @PostMapping("/mypage/modify")
    public String updateMember(@ModelAttribute("member") MyPageDTO dto,
                               @RequestParam(required = false) String currentPassword,
                               @RequestParam(required = false) String newPassword,
                               @RequestParam(required = false) String confirmPassword) {

        // ✅ 테스트용으로 memberId도 강제로 맞춰줌
        dto.setId(52L);

        // 1. 이름, 전화번호 수정
        myPageService.updateMember(dto);

        // 2. 비밀번호 입력이 있는 경우만 처리
        if (currentPassword != null && !currentPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            myPageService.updatePassword(dto.getId(), currentPassword, newPassword);
        }

        return "redirect:/mypage";
    }

    // 비밀번호 검증
    @PostMapping("/mypage/check-password")
    @ResponseBody
    public boolean checkCurrentPassword(@RequestParam String currentPassword) {

//        TODO : 로그인 활성화 되면
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Member member = (Member) authentication.getPrincipal();
//        Long memberId = member.getId();

        Long memberId = 52L; // 로그인 전 임시

        return myPageService.checkCurrentPassword(memberId, currentPassword);
    }

    // 탈퇴 처리
    @PostMapping("/mypage/withdraw")
    public String withdrawMember(HttpSession session) {
        System.out.println(">>> 탈퇴 컨트롤러 진입 확인");

        Long memberId = 52L; // 로그인 연동 전 임시

        // 상태 변경 (WITHDRAWN)
        myPageService.withdrawMember(memberId);

        // 세션 만료 처리 (로그아웃 효과)
        session.invalidate();

        return "redirect:/login";
    }

}


