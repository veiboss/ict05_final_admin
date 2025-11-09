package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.fcm.service.HqAlertStubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fcm/hq-alert/test")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
public class HqAlertStubController {

	private final HqAlertStubService stub;

	@PostMapping("/stock-low")
	public ResponseEntity<?> stockLow(@RequestParam String materialName,
									  @RequestParam long qty,
									  @RequestParam long threshold) {
		String id = stub.sendHqStockLow(materialName, qty, threshold);
		return ResponseEntity.ok(Map.of("messageId", id));
	}

	@PostMapping("/expire-soon")
	public ResponseEntity<?> expireSoon(@RequestParam String materialName,
										@RequestParam int days,
										@RequestParam String lot) {
		String id = stub.sendHqExpireSoon(materialName, days, lot);
		return ResponseEntity.ok(Map.of("messageId", id));
	}
}
