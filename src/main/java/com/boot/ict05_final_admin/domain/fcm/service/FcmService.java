package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.dto.FcmRegisterTokenRequest;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmDeviceToken;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmSendLog;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmTemplate;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmDeviceTokenRepository;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmSendLogRepository;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmDeviceTokenRepository tokenRepository;
    private final FcmTemplateRepository templateRepository;
    private final FcmSendLogRepository logRepository;
    private final ObjectMapper objectMapper;

    /** FirebaseApp은 fcm.enabled=false면 빈이 생성되지 않으므로 선택 주입 */
    @Autowired(required = false)
    @Nullable
    private FirebaseApp firebaseApp;

    /* ======================== Registration ======================== */

    /**
     * HQ 세션 사용자의 토큰을 등록/업서트한다.
     * @param req 클라이언트에서 온 등록 요청
     * @param sessionMemberId 세션에서 추출한 HQ 사용자 ID (우선 적용)
     */
    @Transactional
    public void registerToken(FcmRegisterTokenRequest req, Long sessionMemberId) {
        String token = req.token();
        FcmDeviceToken row = tokenRepository.findByToken(token)
                .orElse(FcmDeviceToken.builder().token(token).build());

        row.setAppType(req.appType());
        row.setPlatform(req.platform());
        row.setDeviceId(req.deviceId());
        row.setMemberIdFk(sessionMemberId != null ? sessionMemberId : req.memberIdFk());
        row.setIsActive(true);
        row.setLastSeenAt(LocalDateTime.now());

        tokenRepository.save(row);
    }

    @Transactional
    public void unregisterToken(String token, Long sessionMemberId) {
        tokenRepository.findByToken(token).ifPresent(row -> {
            // 필요 시 sessionMemberId와 row.memberIdFk 일치 검증
            row.setIsActive(false);
            row.setLastSeenAt(LocalDateTime.now());
        });
    }

    /* ======================== Template Render ======================== */

    @Transactional(readOnly = true)
    public String renderTitle(String templateCode, Map<String, Object> vars) {
        return render(templateCode, true, vars);
    }

    @Transactional(readOnly = true)
    public String renderBody(String templateCode, Map<String, Object> vars) {
        return render(templateCode, false, vars);
    }

    private String render(String code, boolean title, Map<String, Object> vars) {
        FcmTemplate t = templateRepository.findByTemplateCode(code)
                .orElseThrow(() -> new IllegalArgumentException("No template: " + code));
        String src = title ? t.getTitleTemplate() : t.getBodyTemplate();
        if (vars == null || vars.isEmpty()) return src;
        String out = src;
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return out;
    }

    /* ======================== Send (Token / Topic) ======================== */

    public String sendToToken(AppType appType, String token, String title, String body, Map<String, String> data) {
        ensureFirebase();
        try {
            Notification notif = Notification.builder().setTitle(title).setBody(body).build();
            Message msg = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(webpushConfigFor(data))
                    .putAllData(data == null ? Map.of() : data)
                    .build();
            String msgId = FirebaseMessaging.getInstance(firebaseApp).send(msg);
            persistLog(appType, null, token, title, body, data, msgId, null, null, null, null);
            return msgId;
        } catch (Exception ex) {
            log.error("[FCM] sendToToken failed: {}", ex.getMessage(), ex);
            persistLog(appType, null, token, title, body, data, null, ex.getMessage(), null, null, null);
            throw new IllegalStateException(ex);
        }
    }

    public String sendToTopic(AppType appType, String topic, String title, String body, Map<String, String> data) {
        ensureFirebase();
        try {
            Notification notif = Notification.builder().setTitle(title).setBody(body).build();
            Message msg = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(webpushConfigFor(data))
                    .putAllData(data == null ? Map.of() : data)
                    .build();
            String msgId = FirebaseMessaging.getInstance(firebaseApp).send(msg);
            persistLog(appType, topic, null, title, body, data, msgId, null, null, null, null);
            return msgId;
        } catch (Exception ex) {
            log.error("[FCM] sendToTopic failed: {}", ex.getMessage(), ex);
            persistLog(appType, topic, null, title, body, data, null, ex.getMessage(), null, null, null);
            throw new IllegalStateException(ex);
        }
    }

    private void ensureFirebase() {
        if (firebaseApp == null) {
            throw new IllegalStateException("FCM is disabled (no FirebaseApp bean). Set fcm.enabled=true");
        }
    }

    private void persistLog(AppType appType, String topic, String token,
                            String title, String body, Map<String, String> data,
                            String msgId, String error,
                            Long storeId, Long memberId, Long staffId) {
        try {
            FcmSendLog logRow = FcmSendLog.builder()
                    .appType(appType)
                    .topic(topic)
                    .token(token)
                    .title(title)
                    .body(body)
                    .dataJson(data == null ? null : objectToJsonSafe(data))
                    .resultMessageId(msgId)
                    .resultError(error)
                    .storeIdFk(storeId)
                    .memberIdFk(memberId)
                    .staffIdFk(staffId)
                    .build();
            logRepository.save(logRow);
        } catch (Exception e) {
            log.warn("[FCM] persistLog failed: {}", e.getMessage());
        }
    }

    private String objectToJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /** 토픽 구독 (멤버의 활성 토큰 전부를 topic에 구독) */
    public void subscribeToTopic(String topic, Long memberId) {
        ensureFirebase();
        var tokens = tokenRepository.findActiveTokensForHqMember(memberId);
        if (tokens == null || tokens.isEmpty()) {
            log.info("[FCM] subscribe skipped (no active tokens) memberId={}", memberId);
            return;
        }
        // FCM는 1000개 단위 권장 → 청크 처리
        for (int i = 0; i < tokens.size(); i += 1000) {
            var batch = tokens.subList(i, Math.min(i + 1000, tokens.size()));
            try {
                TopicManagementResponse resp =
                        FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(batch, topic);
                log.info("[FCM] subscribeToTopic topic='{}' success={}, failure={}",
                        topic, resp.getSuccessCount(), resp.getFailureCount());
            } catch (Exception e) {
                log.warn("[FCM] subscribeToTopic failed (batch size={}): {}", batch.size(), e.getMessage());
            }
        }
    }

    /** 토픽 구독 해제 */
    public void unsubscribeFromTopic(String topic, Long memberId) {
        ensureFirebase();
        var tokens = tokenRepository.findActiveTokensForHqMember(memberId);
        if (tokens == null || tokens.isEmpty()) {
            log.info("[FCM] unsubscribe skipped (no active tokens) memberId={}", memberId);
            return;
        }
        for (int i = 0; i < tokens.size(); i += 1000) {
            var batch = tokens.subList(i, Math.min(i + 1000, tokens.size()));
            try {
                TopicManagementResponse resp =
                        FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(batch, topic);
                log.info("[FCM] unsubscribeFromTopic topic='{}' success={}, failure={}",
                        topic, resp.getSuccessCount(), resp.getFailureCount());
            } catch (Exception e) {
                log.warn("[FCM] unsubscribeFromTopic failed (batch size={}): {}", batch.size(), e.getMessage());
            }
        }
    }
    /** WebPush 옵션(아이콘/배지/딥링크/TTL/긴급도) 세팅 */
    private WebpushConfig webpushConfigFor(Map<String,String> data) {
        String link = (data != null && data.get("link") != null) ? data.get("link") : "/admin";
        return WebpushConfig.builder()
                // 브라우저 알림 헤더
                .putHeader("TTL", "3600")            // 1시간 캐시
                .putHeader("Urgency", "high")
                // 웹 푸시 알림 비주얼
                .setNotification(WebpushNotification.builder()
                        .setIcon("/admin/images/fcm/toastlab.png")   // 존재하면 유지
                        .setBadge("/admin/images/fcm/badge-72.png")  // 있으면 지정(선택)
                        .build())
                // 알림 클릭 시 열 링크
                .setFcmOptions(WebpushFcmOptions.withLink(link))
                .build();
    }
}
