package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.dto.HqTopic;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HqAlertStubService {

	private final FcmService fcmService;

	public String sendHqStockLow(String materialName, long qty, long threshold) {
		// ✅ 방법 A (또는 아래 주석처리된 방법 B 사용)
		var vars = Map.<String, Object>of(
				"materialName", materialName,
				"qty",          qty,
				"threshold",    threshold
		);
		// 방법 B:
		// Map<String, Object> vars = new HashMap<>();
		// vars.put("materialName", materialName);
		// vars.put("qty", qty);
		// vars.put("threshold", threshold);

		var title = fcmService.renderTitle("HQ_STOCK_LOW", vars);
		var body  = fcmService.renderBody ("HQ_STOCK_LOW", vars);

		Map<String, String> data = new HashMap<>();
		data.put("type", "HQ_STOCK_LOW");
		data.put("materialName", materialName);
		data.put("link", "/admin/inventory/list");

		return fcmService.sendToTopic(AppType.HQ, HqTopic.STOCK_LOW, title, body, data);
	}

	public String sendHqExpireSoon(String materialName, int days, String lot) {
		// ✅ 방법 A
		var vars = Map.<String, Object>of(
				"materialName", materialName,
				"days",         days,
				"lot",          lot
		);
		// 방법 B:
		// Map<String, Object> vars = new HashMap<>();
		// vars.put("materialName", materialName);
		// vars.put("days", days);
		// vars.put("lot", lot);

		var title = fcmService.renderTitle("HQ_EXPIRE_SOON", vars);
		var body  = fcmService.renderBody ("HQ_EXPIRE_SOON", vars);

		Map<String, String> data = new HashMap<>();
		data.put("type", "HQ_EXPIRE_SOON");
		data.put("materialName", materialName);
		data.put("days", String.valueOf(days));
		data.put("lot", lot);
		data.put("link", "/admin/inventory/list");

		return fcmService.sendToTopic(AppType.HQ, HqTopic.EXPIRE_SOON, title, body, data);
	}
}

