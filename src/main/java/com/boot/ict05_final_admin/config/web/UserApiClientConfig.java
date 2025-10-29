package com.boot.ict05_final_admin.config.web;

import com.boot.ict05_final_admin.common.util.CookieUtil;
import com.boot.ict05_final_admin.config.HqProps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class UserApiClientConfig {
    private final HqProps props;
    private final HttpServletRequest request;

    @Bean
    public RestClient userApiClient() {
        return RestClient.builder()
                .baseUrl(props.getUserApiBaseUrl()) // 예: http://localhost:8082/user
                .requestInterceptor((req, body, ex) -> {
                    var at = CookieUtil.read(request, "accessToken");
                    if (at != null && !at.isBlank()) req.getHeaders().setBearerAuth(at);
                    return ex.execute(req, body);
                })
                .build();
    }
}
