package com.boot.ict05_final_admin.domain.fcm.config;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmTemplate;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FCM 템플릿 기본값을 멱등(Upsert)으로 주입한다.
 * - dev에서는 기본 on (matchIfMissing=true)
 * - prod에서는 필요시만 on
 *
 * application.properties 예시:
 *   fcm.seed.templates=true  # dev에서 on
 */
@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "fcm.seed.templates", havingValue = "true", matchIfMissing = true)
public class FcmTemplateDataInitializer implements ApplicationRunner {

	private final FcmTemplateRepository templateRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {

		upsert("HQ_STOCK_LOW",
				"[재고] 본사 재고 부족",
				"{materialName} 재고 {qty} (임계:{threshold})");

		upsert("HQ_EXPIRE_SOON",
				"[유통기한 임박] {materialName}",
				"{days}일 남음 (로트:{lot})");
	}

	private void upsert(String code, String titleTpl, String bodyTpl) {
		FcmTemplate row = templateRepository.findByTemplateCode(code)
				.orElseGet(() -> FcmTemplate.builder().templateCode(code).build());
		row.setTitleTemplate(titleTpl);
		row.setBodyTemplate(bodyTpl);
		templateRepository.save(row);
		log.info("[FCM] template upserted: {}", code);
	}
}