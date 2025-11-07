package com.boot.ict05_final_admin.domain.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.service-account:}")
    private String serviceAccountPath;

    /** fcm.enabled=true 일 때만 FirebaseApp 빈을 등록 (기본은 false 권장) */
    @Bean
    @ConditionalOnProperty(name = "fcm.enabled", havingValue = "true", matchIfMissing = false)
    public FirebaseApp firebaseApp(ResourceLoader resourceLoader) {
        try {
            if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                throw new IllegalStateException(
                        "fcm.enabled=true 인데 fcm.service-account 경로가 비어있습니다. " +
                                "예) classpath:firebase/service-account.json");
            }
            Resource resource = resourceLoader.getResource(serviceAccountPath);
            if (!resource.exists()) {
                throw new IllegalStateException(
                        "Firebase 서비스 계정 파일을 찾을 수 없습니다: " + serviceAccountPath);
            }
            try (InputStream is = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    log.info("[FCM] Initializing FirebaseApp with {}", serviceAccountPath);
                    return FirebaseApp.initializeApp(options);
                }
                return FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init FirebaseApp: " + e.getMessage(), e);
        }
    }
}