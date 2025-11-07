package com.boot.ict05_final_admin.domain.fcm.dto;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.PlatformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FcmRegisterTokenRequest(
        @NotNull AppType appType,
        @NotNull PlatformType platform,
        @NotBlank String token,
        String deviceId,
        Long memberIdFk     // HQ는 세션의 로그인 사용자 ID로 서버에서 override 가능
) { }
