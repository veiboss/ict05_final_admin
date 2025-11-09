package com.boot.ict05_final_admin.domain.fcm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HQ 토픽 사용 정책.
 * - restrict=true 이면 HqTopic enum에 정의된 토픽만 허용 (화이트리스트)
 * - false 이면 패턴 검증만 통과하면 허용
 */
@Component
@ConfigurationProperties(prefix = "fcm.topic")
@Getter @Setter
public class FcmTopicPolicyProperties {
	/** true: HqTopic 화이트리스트로 제한, false: 패턴만 통과하면 허용 */
	private boolean restrict = true;
}
