package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;

import java.util.List;

public interface FcmDeviceTokenRepositoryCustom {
    List<String> findActiveTokensForHqMember(Long memberId);
    List<String> findActiveTokensByAppType(AppType appType, int limit);
}
