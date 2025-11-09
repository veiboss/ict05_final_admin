package com.boot.ict05_final_admin.domain.fcm.dto;

import java.time.LocalDateTime;

public record FcmLogRowDto(
		Long id,
		String topic,
		String token,
		String title,
		String body,
		String resultMessageId,
		String resultError,
		LocalDateTime sentAt
) { }
