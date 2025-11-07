package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.QUnitPrice;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPriceType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UnitPriceRepositoryImpl implements UnitPriceRepositoryCustom {
    private final JPAQueryFactory qf;
    private static final QUnitPrice p = QUnitPrice.unitPrice;

    @Override
    public Optional<UnitPrice> findLatestPurchasePrice(Long materialId, LocalDateTime at) {
        LocalDateTime ts = at != null ? at : LocalDateTime.now();
        UnitPrice row = qf.selectFrom(p)
                .where(
                        p.material.id.eq(materialId),
                        p.type.eq(UnitPriceType.PURCHASE),
                        p.validFrom.loe(ts),
                        p.validTo.isNull().or(p.validTo.gt(ts))   // 기간 종료가 없거나, ts 이후
                )
                .orderBy(p.validFrom.desc(), p.id.desc())     // validFrom 기준 최신
                .limit(1)
                .fetchOne();
        return Optional.ofNullable(row);
    }

    @Override
    public List<UnitPrice> historyPurchasePrice(Long materialId, int limit) {
        return qf.selectFrom(p)
                .where(
                        p.material.id.eq(materialId),
                        p.type.eq(UnitPriceType.PURCHASE)
                )
                .orderBy(p.validFrom.desc(), p.id.desc())
                .limit(limit)
                .fetch();
    }
}
