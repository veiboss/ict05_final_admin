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

/**
 * 관리자 마이페이지 관련 컨트롤러
 * <p>
 * 마이페이지 상세 조회, 수정 화면을 제공한다.
 * 비밀번호 변경, 탈퇴 기능을 지원한다.
 */
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

    /**
     * 비밀번호 검증 (AJAX 요청)
     *
     * <p>
     * 현재 비밀번호가 DB에 저장된 값과 일치하는지 확인한다.<br>
     * 클라이언트는 AJAX로 `/mypage/check-password`에 POST 요청을 보내며,
     * 비밀번호가 일치하면 true, 일치하지 않으면 false를 반환한다.
     * </p>
     *
     * @param currentPassword 사용자가 입력한 현재 비밀번호
     * @return 일치 여부 (true = 일치, false = 불일치)
     */
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

    /**
     * 회원 탈퇴 처리
     *
     * <p>
     * 회원의 상태를 'WITHDRAWN'으로 변경하여 비활성화(Soft Delete) 처리한다.<br>
     * 처리 후에는 세션을 무효화하여 로그아웃 상태로 만든다.
     * </p>
     *
     * @param session 현재 사용자의 세션 객체 (로그아웃 처리를 위함)
     * @return 탈퇴 후 리다이렉트 경로 (현재는 로그인 페이지로 이동)
     */
    @PostMapping("/mypage/withdraw")
    public String withdrawMember(HttpSession session) {

        Long memberId = 52L; // 로그인 연동 전 임시

        // 상태 변경 (WITHDRAWN)
        myPageService.withdrawMember(memberId);

        // 세션 만료 처리 (로그아웃 효과)
        session.invalidate();

        return "redirect:/login";
    }

}


