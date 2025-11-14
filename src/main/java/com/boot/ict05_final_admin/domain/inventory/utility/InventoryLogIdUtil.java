package com.boot.ict05_final_admin.domain.inventory.utility;

public final class InventoryLogIdUtil {

    private static final long BASE = 1_000_000_000L;

    private InventoryLogIdUtil() {}

    public static long unwrap(Long logId) {
        if (logId == null) {
            throw new IllegalArgumentException("로그 ID가 null 입니다.");
        }
        long v = logId;
        return (v >= BASE) ? (v % BASE) : v;
    }
}
