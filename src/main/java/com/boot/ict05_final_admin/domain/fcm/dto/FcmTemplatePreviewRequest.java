package com.boot.ict05_final_admin.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record FcmTemplatePreviewRequest(
        @NotBlank String templateCode,
        Map<String, Object> variables
) { }
