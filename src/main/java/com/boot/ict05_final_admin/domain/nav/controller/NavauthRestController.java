package com.boot.ict05_final_admin.domain.nav.controller;

import com.boot.ict05_final_admin.domain.nav.service.NavGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/navauth")
public class NavauthRestController {

    private final NavGateService navGateService;

    @PostMapping("/{id}/enable")
    public ResponseEntity<?> enable(@PathVariable Long id) {
        boolean enabled = navGateService.setEnabled(id, true);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enabled));
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable Long id) {
        boolean enabled = navGateService.setEnabled(id, false);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enabled));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        boolean enabled = navGateService.toggle(id);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enabled));
    }
}
