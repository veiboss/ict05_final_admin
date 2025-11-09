package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.FcmLogRowDto;
import com.boot.ict05_final_admin.domain.fcm.entity.QFcmSendLog;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FcmSendLogRepositoryImpl implements FcmSendLogRepositoryCustom {

	private final JPAQueryFactory query;

	@Override
	@Transactional(readOnly = true)
	public List<FcmLogRowDto> findRecent(int limit) {
		QFcmSendLog l = QFcmSendLog.fcmSendLog;
		return query
				.select(Projections.constructor(FcmLogRowDto.class,
						l.fcmSendLogId,
						l.topic,
						l.token,
						l.title,
						l.body,
						l.resultMessageId,
						l.resultError,
						l.sentAt
				))
				.from(l)
				.orderBy(l.sentAt.desc()) // 최신순
				.limit(limit)
				.setHint("org.hibernate.readOnly", true)
				.setHint("org.hibernate.flushMode", "COMMIT")
				.setHint("javax.persistence.query.timeout", 3000)
				.fetch();
	}
}
