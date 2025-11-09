package com.boot.ict05_final_admin.domain.fcm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FcmPreferenceUpdateRequest(
		Boolean catNotice,
		Boolean catStockLow,
		Boolean catExpireSoon,
		@Min(1) @Max(30) Integer thresholdDays,
		/** true면 변경 즉시 구독/해제 반영 */
		Boolean applySubscriptions
) { }
