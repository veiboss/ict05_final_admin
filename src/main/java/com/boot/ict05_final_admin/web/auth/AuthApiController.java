package com.boot.ict05_final_admin.web.auth;

import com.boot.ict05_final_admin.domain.auth.dto.JoinRequest;
import com.boot.ict05_final_admin.domain.auth.service.AuthGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthApiController {
    private final AuthGateway gateway;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Long>> register(@RequestBody @Valid JoinRequest req){
        Map<String, Long> res = gateway.join((Map<String, Object>) req);
        return ResponseEntity.status(201).body(res);
    }
}
