package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmRegisterTokenRequest;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmTemplatePreviewRequest;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmTestSendRequest;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.service.FcmService;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm") // 컨텍스트패스가 /admin 이므로 최종 /admin/fcm/**
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
public class HqFcmAdminController {

    private final FcmService fcmService;

    /* ===== [NEW] 세션 사용자 이메일→ID 조회에 사용 ===== */
    private final MyPageService myPageService;

    /** 토큰 등록(업서트). 세션 사용자 ID를 자동 매핑 */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody FcmRegisterTokenRequest req,
                                      @AuthenticationPrincipal Object authPrincipal, // 커스텀 Principal일 수도 있으니 Object
                                      Principal principal) {
        Long sessionMemberId = resolveMemberId(authPrincipal, principal);
        fcmService.registerToken(req, sessionMemberId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/register/{token}")
    public ResponseEntity<?> unregister(@PathVariable String token,
                                        @AuthenticationPrincipal Object authPrincipal,
                                        Principal principal) {
        fcmService.unregisterToken(token, resolveMemberId(authPrincipal, principal));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/template/preview")
    public ResponseEntity<?> preview(@Valid @RequestBody FcmTemplatePreviewRequest req) {
        String title = fcmService.renderTitle(req.templateCode(), req.variables());
        String body  = fcmService.renderBody (req.templateCode(), req.variables());
        return ResponseEntity.ok(Map.of("title", title, "body", body));
    }

    @PostMapping("/send/test")
    public ResponseEntity<?> sendTest(@Valid @RequestBody FcmTestSendRequest req) {
        String id = req.topic()
                ? fcmService.sendToTopic(AppType.HQ, req.tokenOrTopic(), req.title(), req.body(), req.data())
                : fcmService.sendToToken(AppType.HQ, req.tokenOrTopic(), req.title(), req.body(), req.data());
        return ResponseEntity.ok(Map.of("messageId", id));
    }

    /* ===================== helpers ===================== */

    /**
     * 세션 Principal에서 memberId(Long)를 최대한 관용적으로 추출한다.
     * - 커스텀 Principal에 getMemberId()/getId()가 있으면 반영
     * - 그 외엔 java.security.Principal의 username만 보유 → 매핑이 필요하면 여기서 보강
     */
    /**
     * 세션 Principal에서 memberId(Long)를 추출한다.
     * 1) 커스텀 Principal이 Member라면 그대로 ID
     * 2) UserDetails면 username(email)로 조회
     * 3) java.security.Principal의 name도 이메일로 보고 조회
     * 실패 시 null
     */
    private Long resolveMemberId(Object authPrincipal, Principal plain) {
        // (A) SecurityContext에서 한 번 더 가져와 확실히 principal 확보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object p = (authPrincipal != null) ? authPrincipal :
                (authentication != null ? authentication.getPrincipal() : null);

        // (1) 커스텀 principal이 Member 타입인 경우
        if (p instanceof Member m) {
            return m.getId();
        }

        // (2) UserDetails인 경우: username(email)로 조회
        if (p instanceof UserDetails ud) {
            try {
                return myPageService.findByEmail(ud.getUsername()).getId();
            } catch (Exception ignore) { /* not found */ }
        }

        // (3) java.security.Principal 에만 값이 있는 경우
        if (plain != null && plain.getName() != null && !plain.getName().isBlank()) {
            try {
                return myPageService.findByEmail(plain.getName()).getId();
            } catch (Exception ignore) { /* not found */ }
        }

        // (4) 혹시 모를 케이스를 위해 리플렉션도 마지막 시도
        Long byReflection = extractIdByReflection(p).orElse(null);
        return byReflection;
    }

    private Optional<Long> extractIdByReflection(Object principal) {
        if (principal == null) return Optional.empty();
        for (String m : new String[]{"getMemberId", "getId", "memberId", "id"}) {
            try {
                Method method = principal.getClass().getMethod(m);
                Object v = method.invoke(principal);
                if (v instanceof Long l) return Optional.of(l);
                if (v instanceof Number n) return Optional.of(n.longValue());
                if (v instanceof String s && !s.isBlank()) return Optional.of(Long.parseLong(s));
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) { }
        }
        return Optional.empty();
    }

    @PostMapping("/topic/subscribe")
    public ResponseEntity<?> subscribeMyTokens(@RequestParam String topic, Principal principal) {
        Long memberId = extractMemberId(principal);
        fcmService.subscribeToTopic(topic, memberId);   // ← 이름 일치!
        return ResponseEntity.ok(Map.of("ok", true, "topic", topic));
    }

    @PostMapping("/topic/unsubscribe")
    public ResponseEntity<?> unsubscribeMyTokens(@RequestParam String topic, Principal principal) {
        Long memberId = extractMemberId(principal);
        fcmService.unsubscribeFromTopic(topic, memberId); // ← 이름 일치!
        return ResponseEntity.ok(Map.of("ok", true, "topic", topic));
    }

    /** 세션 사용자 ID 추출 (Member → ID / UserDetails → email로 조회) */
    private Long extractMemberId(Principal plain) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object p = (auth != null ? auth.getPrincipal() : null);

        if (p instanceof Member m) return m.getId();

        if (p instanceof UserDetails ud) {
            return myPageService.findByEmail(ud.getUsername()).getId();
        }

        if (plain != null && plain.getName() != null && !plain.getName().isBlank()) {
            return myPageService.findByEmail(plain.getName()).getId();
        }

        throw new IllegalStateException("로그인 사용자를 식별할 수 없습니다.");
    }
}
