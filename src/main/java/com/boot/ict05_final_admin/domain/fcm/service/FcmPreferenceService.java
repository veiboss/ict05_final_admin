package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmPreference;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmPreferenceService {

	private final FcmPreferenceRepository prefRepo;

	@Transactional
	public FcmPreference upsertForHqMember(Long memberId,
										   Boolean catNotice,
										   Boolean catStockLow,
										   Boolean catExpireSoon,
										   Integer thresholdDays) {

		FcmPreference row = prefRepo.findFirstByAppTypeAndMemberIdFk(AppType.HQ, memberId)
				.orElseGet(() -> FcmPreference.builder()
						.appType(AppType.HQ)
						.memberIdFk(memberId)
						.build());

		if (catNotice     != null) row.setCatNotice(catNotice);
		if (catStockLow   != null) row.setCatStockLow(catStockLow);
		if (catExpireSoon != null) row.setCatExpireSoon(catExpireSoon);
		if (thresholdDays != null) row.setThresholdDays(thresholdDays);

		return prefRepo.save(row);
	}

	@Transactional(readOnly = true)
	public FcmPreference getForHqMember(Long memberId) {
		return prefRepo.findFirstByAppTypeAndMemberIdFk(AppType.HQ, memberId).orElse(null);
	}
}
