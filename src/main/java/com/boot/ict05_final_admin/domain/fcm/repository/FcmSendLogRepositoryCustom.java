package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.FcmLogRowDto;

import java.util.List;

public interface FcmSendLogRepositoryCustom {
	List<FcmLogRowDto> findRecent(int limit);
}
