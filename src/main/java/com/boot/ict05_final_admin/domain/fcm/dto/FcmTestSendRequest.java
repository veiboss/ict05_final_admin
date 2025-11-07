package com.boot.ict05_final_admin.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record FcmTestSendRequest(
        @NotBlank String tokenOrTopic,
        boolean topic,                 // true면 토픽, false면 토큰
        @NotBlank String title,
        @NotBlank String body,
        Map<String, String> data
) { }
