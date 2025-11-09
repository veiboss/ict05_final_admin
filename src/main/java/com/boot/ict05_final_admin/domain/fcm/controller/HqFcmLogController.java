package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.fcm.repository.FcmSendLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fcm/logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
public class HqFcmLogController {

	private final FcmSendLogRepository logRepo;

	@GetMapping
	public String logs(@RequestParam(defaultValue = "100") int limit, Model model) {
		int safe = Math.max(1, Math.min(500, limit));
		model.addAttribute("rows", logRepo.findRecent(safe));
		model.addAttribute("limit", safe);
		return "fcm/logs";
	}
}
