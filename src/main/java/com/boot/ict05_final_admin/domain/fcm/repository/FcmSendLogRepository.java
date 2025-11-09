package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmSendLogRepository extends JpaRepository<FcmSendLog, Long>, FcmSendLogRepositoryCustom { }
