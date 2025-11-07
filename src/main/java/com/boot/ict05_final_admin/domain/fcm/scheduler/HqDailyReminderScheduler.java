package com.boot.ict05_final_admin.domain.fcm.scheduler;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
// import org.springframework.scheduling.annotation.Scheduled; // 필요 시 활성화

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HqDailyReminderScheduler {

    private final FcmService fcmService;

    // @Scheduled(cron = "0 10 9 * * *")
    public void sendMorningReminder() {
        try {
            fcmService.sendToTopic(
                    AppType.HQ, "hq-all",
                    "[본사] 아침 리마인드", "금일 재고/공지 확인을 진행해주세요.",
                    Map.of("type", "HQ_REMINDER", "link", "/admin/dashboard"));
        } catch (Exception e) {
            log.warn("Failed to send morning reminder: {}", e.getMessage());
        }
    }
}
