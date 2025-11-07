package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTemplateRepository extends JpaRepository<FcmTemplate, Long> {
    Optional<FcmTemplate> findByTemplateCode(String templateCode);
}
