package com.boot.ict05_final_admin.domain.notice.entity;

public enum NoticePriority {
    NOMAL("일반"),
    IMPORTANT("중요"),
    EMERGENCY("긴급");

    private final String description;

    NoticePriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
