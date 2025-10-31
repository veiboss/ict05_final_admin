package com.boot.ict05_final_admin.domain.myPage.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.myPage.dto.MyPageDTO;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 관리자 마이페이지 관련 컨트롤러
 * <p>
 * 마이페이지 상세 조회, 수정 화면을 제공한다.
 * 비밀번호 변경, 탈퇴 기능을 지원한다.
 */
@Controller
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "회원 프로필 조회 및 수정 API")
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 조회
     * - 로그인 전에는 임시 memberId로 테스트
     * - 로그인 연동 시 SecurityContext에서 memberId 자동 추출
     */
    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 상세 조회", description = "회원의 프로필 정보를 조회한다.")
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
     * 회원 수정 폼 페이지
     *
     * @param model Thymeleaf에 전달할 모델
     * @return 수정 페이지 뷰
     */
    @GetMapping("/mypage/modify")
    @Operation(summary = "마이페이지 수정 폼", description = "기존 회원 데이터를 불러와 수정 입력 폼을 표시한다.")
    public String modifyForm(Model model) {

        // Long memberId = ((Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        Long memberId = 52L; // 로그인 연동 전 임시 ID
        MyPageDTO dto = myPageService.getMyPage(memberId);
        model.addAttribute("member", dto);
        return "mypage/modify";
    }

    /**
     * 회원 정보 수정 처리
     *
     * @param dto            수정할 회원 정보 DTO
     * @param profileImage   업로드할 새 프로필 이미지
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     * @param confirmPassword 새 비밀번호 확인
     * @return 수정 후 마이페이지 상세 페이지로 리다이렉트
     */
    @PostMapping("/mypage/modify")
    @Operation(summary = "마이페이지 수정 처리", description = "이름, 전화번호, 비밀번호, 프로필 이미지를 한 번에 수정한다.")
    public String updateMember(@ModelAttribute("member") MyPageDTO dto,
                               @RequestParam(value = "memberImage", required = false) MultipartFile memberImage,
                               @RequestParam(required = false) String currentPassword,
                               @RequestParam(required = false) String newPassword,
                               @RequestParam(required = false) String confirmPassword) throws IOException {

        // 테스트용으로 memberId도 강제로 맞춰줌
        dto.setId(52L);

        // 1. 프로필 이미지 업로드
        if (memberImage != null && !memberImage.isEmpty()) {
            // 실제 서버 저장 경로
            String uploadDir = "D:/ict05_uploads/profile/"; // 로컬 테스트용 절대경로
            String fileName = UUID.randomUUID() + "_" + memberImage.getOriginalFilename();

            Path path = Paths.get(uploadDir, fileName);
            Files.createDirectories(path.getParent());
            memberImage.transferTo(path.toFile()); // 실제 파일 저장

            // 브라우저 접근용 URL (WebConfig에서 매핑됨)
            dto.setMemberImagePath("/uploads/profile/" + fileName);
        }

        // 2. 이름, 전화번호 수정
        myPageService.updateMember(dto);

        // 3. 비밀번호 입력이 있는 경우만 처리
        if (currentPassword != null && !currentPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            myPageService.updatePassword(dto.getId(), currentPassword, newPassword);
        }

        return "redirect:/mypage";
    }

    /**
     * 비밀번호 검증 (AJAX)
     *
             * @param currentPassword 입력된 현재 비밀번호
     * @return 일치 여부 (true = 일치)
     */
    @PostMapping("/mypage/check-password")
    @ResponseBody
    @Operation(summary = "비밀번호 검증", description = "현재 비밀번호가 DB에 저장된 값과 일치하는지 확인한다.")
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
     * @param session 현재 세션
     * @return 로그인 페이지로 리다이렉트
     */
    @PostMapping("/mypage/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원 상태를 'WITHDRAWN'으로 변경하고 세션을 만료시킨다.")
    public String withdrawMember(HttpSession session) {

        Long memberId = 52L; // 로그인 연동 전 임시

        // 상태 변경 (WITHDRAWN)
        myPageService.withdrawMember(memberId);

        // 세션 만료 처리 (로그아웃 효과)
        session.invalidate();

        return "redirect:/login";
    }

}


