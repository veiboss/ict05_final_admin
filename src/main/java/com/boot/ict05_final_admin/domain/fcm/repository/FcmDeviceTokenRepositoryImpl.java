package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.QFcmDeviceToken;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FcmDeviceTokenRepositoryImpl implements FcmDeviceTokenRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    @Transactional(readOnly = true)
    public List<String> findActiveTokensForHqMember(Long memberId) {
        QFcmDeviceToken t = QFcmDeviceToken.fcmDeviceToken;
        return query
                .select(t.token)
                .from(t)
                .where(
                        t.appType.eq(AppType.HQ),
                        t.isActive.isTrue(),
                        t.memberIdFk.eq(memberId)
                )
                .orderBy(Expressions.stringTemplate("NULL").asc()) // ORDER BY NULL
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("javax.persistence.query.timeout", 3000)
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findActiveTokensByAppType(AppType appType, int limit) {
        QFcmDeviceToken t = QFcmDeviceToken.fcmDeviceToken;
        return query
                .select(t.token)
                .from(t)
                .where(t.appType.eq(appType), t.isActive.isTrue())
                .limit(limit)
                .orderBy(Expressions.stringTemplate("NULL").asc())
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("javax.persistence.query.timeout", 3000)
                .fetch();
    }
}
