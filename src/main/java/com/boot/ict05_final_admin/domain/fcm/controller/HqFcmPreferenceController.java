package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmPreferenceUpdateRequest;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmPreference;
import com.boot.ict05_final_admin.domain.fcm.service.FcmPreferenceService;
import com.boot.ict05_final_admin.domain.fcm.service.FcmService;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/fcm/pref")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
public class HqFcmPreferenceController {

	private final FcmPreferenceService prefService;
	private final FcmService fcmService;
	private final MyPageService myPageService;

	@GetMapping("/me")
	public ResponseEntity<?> getMine(Principal principal, Authentication auth) {
		Long memberId = extractMemberId(principal, auth);
		FcmPreference p = prefService.getForHqMember(memberId);
		return ResponseEntity.ok(Map.of(
				"memberId", memberId,
				"catNotice",    p != null ? p.getCatNotice()    : true,
				"catStockLow",  p != null ? p.getCatStockLow()  : true,
				"catExpireSoon",p != null ? p.getCatExpireSoon(): true,
				"thresholdDays",p != null ? p.getThresholdDays(): 3
		));
	}

	@PostMapping("/me")
	public ResponseEntity<?> updateMine(@Valid @RequestBody FcmPreferenceUpdateRequest req,
										Principal principal, Authentication auth) {
		Long memberId = extractMemberId(principal, auth);
		FcmPreference saved = prefService.upsertForHqMember(
				memberId, req.catNotice(), req.catStockLow(), req.catExpireSoon(), req.thresholdDays());

		// (선택) 즉시 토픽 구독/해제 반영 — HQ 2종만
		boolean apply = req.applySubscriptions() == null || Boolean.TRUE.equals(req.applySubscriptions());
		if (apply) {
			if (req.catStockLow() != null) {
				if (req.catStockLow()) fcmService.subscribeToTopic("hq-stock-low", memberId);
				else fcmService.unsubscribeFromTopic("hq-stock-low", memberId);
			}
			if (req.catExpireSoon() != null) {
				if (req.catExpireSoon()) fcmService.subscribeToTopic("hq-expire-soon", memberId);
				else fcmService.unsubscribeFromTopic("hq-expire-soon", memberId);
			}
		}
		return ResponseEntity.ok(Map.of("ok", true, "prefId", saved.getFcmPreferenceId()));
	}

	/** ✅ 기존 HqFcmAdminController와 동일한 안전한 방식으로 memberId 해석 */
	private Long extractMemberId(Principal principal, Authentication auth) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		Object p = (auth != null && auth.getPrincipal() != null)
				? auth.getPrincipal()
				: (authentication != null ? authentication.getPrincipal() : null);

		if (p instanceof Member m) return m.getId();

		if (p instanceof UserDetails ud) {
			return myPageService.findByEmail(ud.getUsername()).getId();
		}

		if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
			return myPageService.findByEmail(principal.getName()).getId();
		}

		throw new IllegalStateException("로그인 사용자를 식별할 수 없습니다.");
	}
}
