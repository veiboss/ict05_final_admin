package com.boot.ict05_final_admin.domain.fcm.dto;

import java.util.Arrays;

public enum HqTopic {
	HQ_ALL("hq-all"),
	STOCK_LOW("hq-stock-low"),
	EXPIRE_SOON("hq-expire-soon");

	private final String value;
	HqTopic(String value) { this.value = value; }
	public String value() { return value; }

	public static boolean isAllowed(String topic) {
		return Arrays.stream(values()).anyMatch(t -> t.value.equals(topic));
	}

}