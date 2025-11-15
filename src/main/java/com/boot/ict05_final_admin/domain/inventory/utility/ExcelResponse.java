package com.boot.ict05_final_admin.domain.inventory.utility;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * XLSX 바이너리 응답 헬퍼
 *
 * @author 김주연
 * @since 2025-11-12
 */
public final class ExcelResponse {

    private ExcelResponse() {}

    public static ResponseEntity<byte[]> ok(byte[] bytes, String filename) {
        String fallback = ExcelFilename.fallbackAscii(filename);
        String encoded = ExcelFilename.encodeRFC5987(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.set(HttpHeaders.PRAGMA, "no-cache");
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded);

        return ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(bytes);
    }
}
