package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QStore;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReceiveOrderRepositoryImpl implements ReceiveOrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        QReceiveOrder receiveOrder = QReceiveOrder.receiveOrder;
        QStore store = QStore.store;

        // 데이터 목록 조회
        List<ReceiveOrderListDTO> content = queryFactory
                .select(Projections.fields(ReceiveOrderListDTO.class,
                        receiveOrder.id,
                        receiveOrder.store.name.as("storeName"),
                        receiveOrder.store.location.as("storeLocation"),
                        receiveOrder.orderCode,
                        receiveOrder.orderDate,
                        receiveOrder.status,
                        receiveOrder.totalPrice,
                        receiveOrder.priority,
                        receiveOrder.remark,
                        receiveOrder.supplier,
                        receiveOrder.deliveryDate,
                        receiveOrder.actualDeliveryDate
                ))
                .from(receiveOrder)
                .join(receiveOrder.store, store)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, receiveOrder)
                )
                .orderBy(receiveOrder.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(receiveOrder.count())
                .from(receiveOrder)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, receiveOrder)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


    private BooleanExpression eqOrderCode(ReceiveOrderSearchDTO receiveOrderSearchDTO, QReceiveOrder receiveOrder) {
        if (receiveOrderSearchDTO.getType() == null || receiveOrderSearchDTO.getS() == null) {
            return null;
        }

        String keyword = receiveOrderSearchDTO.getS();

        switch (receiveOrderSearchDTO.getType()) {
            case "orderCode":
                return receiveOrder.orderCode.containsIgnoreCase(keyword);
            case "storeName":
                return receiveOrder.store.name.containsIgnoreCase(keyword);
            case "storeLocation":
                return receiveOrder.store.location.containsIgnoreCase(keyword);
            default:
                return null;
        }
    }

    @Override
    public long countReceive(ReceiveOrderSearchDTO receiveOrederSearchDTO) {
        QReceiveOrder receiveOrder = QReceiveOrder.receiveOrder;

        long total = queryFactory
                .select(receiveOrder.count())
                .from(receiveOrder)
                .where(
                        eqOrderCode(receiveOrederSearchDTO, receiveOrder)
                )
                .fetchOne();

        return total;
    }
}
