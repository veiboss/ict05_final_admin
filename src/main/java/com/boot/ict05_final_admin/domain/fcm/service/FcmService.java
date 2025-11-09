package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.config.FcmTopicPolicyProperties;
import com.boot.ict05_final_admin.domain.fcm.config.FcmWebpushProperties;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmRegisterTokenRequest;
import com.boot.ict05_final_admin.domain.fcm.dto.HqTopic;
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
    private final FcmTopicPolicyProperties topicProps;

    /** NEW: WebPush 설정 주입 */
    private final FcmWebpushProperties webpushProps;

    /** FirebaseApp은 fcm.enabled=false면 빈이 생성되지 않으므로 선택 주입 */
    @Autowired(required = false)
    @Nullable
    private FirebaseApp firebaseApp;

    // ① 토픽 검증(허용 문자/길이)
    private static final java.util.regex.Pattern TOPIC_PATTERN = java.util.regex.Pattern.compile("^[a-z0-9-]{1,64}$");

    private String validateTopic(String topic) {
        if (topic == null) throw new IllegalArgumentException("Invalid topic name: null");
        topic = topic.trim().toLowerCase();
        if (!TOPIC_PATTERN.matcher(topic).matches()) {
            throw new IllegalArgumentException("Invalid topic pattern: " + topic);
        }
        // 화이트리스트(선택 정책) 적용
        if (topicProps.isRestrict() && !HqTopic.isAllowed(topic)) {
            throw new IllegalArgumentException("Topic not allowed in HQ: " + topic);
        }
        return topic;
    }

    // ② data 1KB 제한: 초과 시 최소 필드만 유지(link, type)
    private Map<String,String> sanitizeData(Map<String,String> data) {
        if (data == null || data.isEmpty()) return Map.of();
        try {
            byte[] raw = objectMapper.writeValueAsBytes(data);
            if (raw.length <= 1024) return data;
        } catch (Exception ignore) { return data; }

        Map<String,String> slim = new java.util.HashMap<>();
        if (data.containsKey("type")) slim.put("type", data.get("type"));
        if (data.containsKey("link")) slim.put("link", data.get("link"));
        return slim;
    }

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

    /** ⬇️ UPDATED: 공통 빌더 사용 + 설정 기반 WebpushConfig */
    public String sendToToken(AppType appType, String token, String title, String body, Map<String, String> data) {
        ensureFirebase();
        try {
            Map<String, String> safeData = sanitizeData(data);
            Message msg = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(webpushConfigFor(safeData))
                    .putAllData(safeData)
                    .build();

            String msgId = FirebaseMessaging.getInstance(firebaseApp).send(msg);
            persistLog(appType, null, token, title, body, safeData, msgId, null, null, null, null);
            return msgId;
        } catch (Exception ex) {
            log.error("[FCM] sendToToken failed: {}", ex.getMessage(), ex);
            persistLog(appType, null, token, title, body, data, null, ex.getMessage(), null, null, null);
            throw new IllegalStateException(ex);
        }
    }

    /** 공통 빌더 사용 + 설정 기반 WebpushConfig */
    public String sendToTopic(AppType appType, String topic, String title, String body, Map<String, String> data) {
        ensureFirebase();
        try {
            topic = validateTopic(topic);
            Map<String, String> safeData = sanitizeData(data);
            Message msg = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(webpushConfigFor(safeData))
                    .putAllData(safeData)
                    .build();

            String msgId = FirebaseMessaging.getInstance(firebaseApp).send(msg);
            persistLog(appType, topic, null, title, body, safeData, msgId, null, null, null, null);
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
        topic = validateTopic(topic);
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
        topic = validateTopic(topic);
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

    /** 설정 기반 WebpushConfig(아이콘/배지/TTL/긴급도/딥링크) */
    private WebpushConfig webpushConfigFor(Map<String, String> data) {
        String link = (data != null && data.get("link") != null && !data.get("link").isBlank())
                ? data.get("link")
                : webpushProps.getDefaultLink();

        // TTL/Urency 헤더는 Push Service(브라우저 서버)에 전달됨
        return WebpushConfig.builder()
                .putHeader("TTL", String.valueOf(Math.max(0, webpushProps.getTtlSeconds())))
                .putHeader("Urgency", webpushProps.getUrgency())
                .setNotification(
                        WebpushNotification.builder()
                                .setIcon(webpushProps.getIcon())
                                .setBadge(webpushProps.getBadge())
                                .build()
                )
                .setFcmOptions(WebpushFcmOptions.withLink(link))
                .build();
    }

    // 오버로드 메서드들
    public String sendToTopic(AppType appType, HqTopic topic, String title, String body, Map<String, String> data) {
        return sendToTopic(appType, topic.value(), title, body, data);
    }

    public void subscribeToTopic(HqTopic topic, Long memberId) {
        subscribeToTopic(topic.value(), memberId);
    }

    public void unsubscribeFromTopic(HqTopic topic, Long memberId) {
        unsubscribeFromTopic(topic.value(), memberId);
    }

}
