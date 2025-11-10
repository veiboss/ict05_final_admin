package com.boot.ict05_final_admin.domain.receiveOrder.service;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 본사 ↔ 가맹점 간 발주/수주 상태 동기화 서비스
 *
 * <p>본사에서 수주 상태 변경 시 가맹점 발주 상태도 자동 반영하고,</p>
 * <p>가맹점에서 상태가 바뀌면 본사로 반영할 수 있다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderSyncService {

    private final RestTemplate restTemplate;

    /**
     * HQ → STORE 상태 동기화
     *
     * @param orderCode 수주 코드 (가맹점 발주 코드와 동일)
     * @param status    새 상태 (예: SHIPPING, DELIVERED, CANCELED)
     */
    public void syncFromHQ(String orderCode, ReceiveOrderStatus status) {
        try {
            // 가맹점 서버 API 주소 (배포 시 환경별로 변경)
            String storeApiUrl = "http://localhost:8082/api/purchase/sync/status";

            // 요청 파라미터 구성
            String url = storeApiUrl + "?orderCode=" + orderCode + "&status=" + status.name();

            log.info("🔄 HQ → Store 상태 동기화 요청: {}", url);

            // 요청 전송
            ResponseEntity<Void> response =
                    restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ 가맹점에 상태 동기화 성공: {} → {}", orderCode, status);
            } else {
                log.warn("⚠️ 가맹점 상태 동기화 실패: {} (HTTP {})", orderCode, response.getStatusCodeValue());
            }

        } catch (Exception e) {
            log.error("🚨 가맹점 상태 동기화 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * STORE → HQ 상태 동기화
     *
     * @param orderCode 발주 코드 (본사 수주 코드와 동일)
     * @param status    가맹점에서 전달된 상태
     */
    public void syncFromStore(String orderCode, String status) {
        String url = "http://localhost:8081/api/receive/sync/status";
        try {
            log.info("🔄 Store → HQ 동기화 요청: code={}, status={}", orderCode, status);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("orderCode", orderCode);
            params.add("status", status);

            restTemplate.put(url + "?orderCode=" + orderCode + "&status=" + status, null);
            log.info("🏪 [STORE → HQ] 상태 동기화 성공: {} → {}", orderCode, status);
        } catch (Exception e) {
            log.error("❌ [STORE → HQ] 상태 동기화 실패: {} → {}, {}", orderCode, status, e.getMessage());
        }
    }

    /**
     * 가맹점 → 본사 상태 동기화 (가맹점 서버에서 실행)
     *
     * <p>가맹점에서 상태 변경 발생 시 본사 API를 호출한다.</p>
     */
    public void syncToHQ(String orderCode, String status) {
        String url = "http://localhost:8082/user/api/purchase/sync/status";
        try {
            log.info("🏪 [STORE → HQ] 상태 동기화 요청: code={}, status={}", orderCode, status);

            restTemplate.put(url + "?orderCode=" + orderCode + "&status=" + status, null);

            log.info("✅ [STORE → HQ] 상태 동기화 성공: {} → {}", orderCode, status);
        } catch (Exception e) {
            log.error("❌ [STORE → HQ] 상태 동기화 실패: {} → {}, {}", orderCode, status, e.getMessage());
        }
    }

}
