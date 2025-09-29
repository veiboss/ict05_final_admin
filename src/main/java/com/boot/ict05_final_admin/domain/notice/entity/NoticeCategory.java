package com.boot.ict05_final_admin.domain.notice.entity;

public enum NoticeCategory {
    NOMAL("일반"),
    SYSTEM("시스템"),
    EVENT("이벤트"),
    POLICY("공지");

    private final String description;

    NoticeCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
