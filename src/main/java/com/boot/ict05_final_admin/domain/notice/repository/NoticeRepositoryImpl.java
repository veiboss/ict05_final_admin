package com.boot.ict05_final_admin.domain.notice.repository;

<<<<<<< HEAD
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
=======
import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.entity.QNotice;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
>>>>>>> e0564bad210a88f7e3e32abb726c8bf65e0fcb88
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
<<<<<<< HEAD
=======

    @Override
    public Page<NoticeListDTO> listNotice(String writer, Pageable pageable) {
        QNotice notice = QNotice.notice;

        // 데이터 목록 조회
        List<NoticeListDTO> content = queryFactory
                .select(Projections.constructor(NoticeListDTO.class,
                        notice.id
                        )) // member.name 매핑
                .from(notice)
                .where(
                )
                .orderBy(notice.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(notice.count())
                .from(notice)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
>>>>>>> e0564bad210a88f7e3e32abb726c8bf65e0fcb88
}
