package com.boot.ict05_final_admin.domain.order.entity;

/** 주문 형태 (DB에는 영문 소문자 코드 저장: visit/takeout/delivery) */
public enum OrderType {
    VISIT("visit", "방문"),
    TAKEOUT("takeout", "포장"),
    DELIVERY("delivery", "배달");

    private final String code;   // DB 저장값
    private final String label;  // 한글 라벨

    OrderType(String code, String label) {
        this.code = code; this.label = label;
    }
    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static OrderType fromCode(String code) {
        for (OrderType v : values()) {
            if (v.code.equals(code)) return v;
        }
        throw new IllegalArgumentException("Unknown OrderType code=" + code);
    }
}