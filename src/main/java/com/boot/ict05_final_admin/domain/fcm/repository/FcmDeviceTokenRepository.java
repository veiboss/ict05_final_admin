package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmDeviceTokenRepository
        extends JpaRepository<FcmDeviceToken, Long>, FcmDeviceTokenRepositoryCustom {

    Optional<FcmDeviceToken> findByToken(String token);
}
