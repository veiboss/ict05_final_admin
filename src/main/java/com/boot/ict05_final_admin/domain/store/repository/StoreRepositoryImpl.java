package com.boot.ict05_final_admin.domain.store.repository;

import com.boot.ict05_final_admin.domain.store.dto.StoreListDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreSearchDTO;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public Page<StoreListDTO> listStore(StoreSearchDTO storeSearchDTO, Pageable pageable) {
        QStore store = QStore.store;

        // 데이터 목록 조회
        List<StoreListDTO> content = queryFactory
                .select(Projections.fields(StoreListDTO.class,
                       store.id.as("storeId"),
                        store.name.as("storeName"),
                        store.status.as("storeStatus"),
                       // store.storeOwnerName,
                        store.phone.as("storePhone"),
                        store.monthlySales.as("storeMonthlySales")
                        //store.storeTotalEmployees
                )) // member.name 매핑
                .from(store)
                .where(
                        eqStoreName(storeSearchDTO, store)
                )
                .orderBy(store.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        eqStoreName(storeSearchDTO, store)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqStoreName(StoreSearchDTO storeSearchDTO, QStore store) {
        if (storeSearchDTO.getKeyword() == null) {
            return null; // 조건 없음
        }

        String keyword = storeSearchDTO.getKeyword();

        switch (storeSearchDTO.getType()) {
            case "storeName":
                return store.name.containsIgnoreCase(keyword);
            default:
                return null;
        }
    }


    @Override
    public long countStore(StoreSearchDTO storeSearchDTO) {
        QStore store = QStore.store;

        long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        eqStoreName(storeSearchDTO, store)
                )
                .fetchOne();

        return total;
    }
}
