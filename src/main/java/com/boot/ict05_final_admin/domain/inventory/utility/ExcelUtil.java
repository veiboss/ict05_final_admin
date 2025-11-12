package com.boot.ict05_final_admin.domain.inventory.utility;

import lombok.experimental.UtilityClass;
import java.math.BigDecimal;

@UtilityClass
public class ExcelUtil {
    /** null-safe 문자열 */
    public static String n(String v) { return v == null ? "" : v; }

    /** null-safe 객체→문자열 */
    public static String s(Object v) { return v == null ? "" : String.valueOf(v); }

    /** null-safe 숫자 → double (Number 전체 지원) */
    public static double d(Number v) {
        if (v == null) return 0d;
        if (v instanceof BigDecimal bd) return bd.doubleValue();
        return v.doubleValue();
    }
}
