package com.boot.ict05_final_admin.domain.auth.service;

import com.boot.ict05_final_admin.domain.auth.dto.LoginRequest;
import com.boot.ict05_final_admin.domain.auth.dto.MeResponse;
import com.boot.ict05_final_admin.domain.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthGateway {
    private final RestClient userApiClient;

    public TokenResponse login(LoginRequest req) {
        return userApiClient.post().uri("/login").body(req).retrieve()
                .body(TokenResponse.class);
    }
    public Map<String, Long> join(Map<String, Object> req) {
        return userApiClient.post().uri("/join").body(req).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Long>>() {});
    }
    public MeResponse me() {
        return userApiClient.get().uri("/me").retrieve().body(MeResponse.class);
    }
    public TokenResponse refresh(String refreshToken) {
        return RestClient.create().post()
                .uri(URI.create(userApiClient.toString() + "/jwt/refresh")) // 또는 props.userApiBaseUrl()+"/jwt/refresh"
                .header("X-Refresh-Token", refreshToken)
                .retrieve().body(TokenResponse.class);
    }
}
