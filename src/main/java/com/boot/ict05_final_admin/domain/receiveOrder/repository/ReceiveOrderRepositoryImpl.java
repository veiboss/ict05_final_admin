package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.*;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.*;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReceiveOrderRepositoryImpl implements ReceiveOrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    // 상단 카드 데이터
    @Override
    public ReceiveOrderSummaryDTO getSummary() {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;

        Long totalCount = queryFactory
                .select(ro.id.count())
                .from(ro)
                .where(ro.totalPrice.gt(BigDecimal.ZERO)
                        .and(ro.totalCount.gt(0)))
                .fetchOne();

        Long shippingCount = queryFactory
                .select(ro.id.count())
                .from(ro)
                .where(ro.status.eq(ReceiveOrderStatus.SHIPPING))
                .fetchOne();

        Long urgentCount = queryFactory
                .select(ro.id.count())
                .from(ro)
                .where(ro.priority.eq(ReceiveOrderPriority.URGENT))
                .fetchOne();

        BigDecimal totalAmount = queryFactory
                .select(ro.totalPrice.sum().coalesce(BigDecimal.ZERO))
                .from(ro)
                .where(ro.totalPrice.gt(BigDecimal.ZERO))
                .fetchOne();

        return new ReceiveOrderSummaryDTO(
                totalCount != null ? totalCount : 0L,
                totalAmount != null ? totalAmount : BigDecimal.ZERO,
                shippingCount != null ? shippingCount : 0L,
                urgentCount != null ? urgentCount : 0L
        );
    }

    @Override
    public Optional<ReceiveOrder> findOrderById(Long id) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;

        ReceiveOrder result = queryFactory
                .selectFrom(ro)
                .where(ro.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
        QStore store = QStore.store;

        // details 존재 여부 → exists 서브쿼리
        BooleanExpression hasDetails = JPAExpressions
                .selectOne()
                .from(rod)
                .where(rod.receiveOrder.eq(ro))
                .exists();

        // 데이터 목록 조회
        List<ReceiveOrderListDTO> content = queryFactory
                .select(Projections.fields(ReceiveOrderListDTO.class,
                        ro.id,
                        ro.orderCode,
                        ro.store.name.as("storeName"),
                        ro.store.location.as("storeLocation"),
                        ro.status,
                        ro.priority,
                        ro.totalPrice,
                        ro.totalCount.as("totalCount"),
                        ro.actualDeliveryDate
                ))
                .from(ro)
                .join(ro.store, store)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro),
                        hasDetails                          // 주문 건이 있는 주문만
                )
                .orderBy(ro.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(ro.count())
                .from(ro)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro),
                        hasDetails
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    // 검색 필터 - 가맹점명, 주문번호, 지역
    private BooleanExpression eqOrderCode(ReceiveOrderSearchDTO dto, QReceiveOrder ro) {

        // BooleanExpression condition = null;
        // 기본값 true
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // 상태 필터
        if (dto.getReceiveOrderStatus() != null) {
            condition = condition.and(ro.status.eq(dto.getReceiveOrderStatus()));
        }

        String type = dto.getType();
        String keyword = dto.getS();

        if (keyword == null || keyword.trim().isEmpty()) {
            return condition; // 상태만 필터링
        }

        if (type == null) type = "all";

        switch (type) {
            case "orderCode":
                condition = condition.and(ro.orderCode.containsIgnoreCase(keyword));
                break;
            case "storeName":
                condition = condition.and(ro.store.name.containsIgnoreCase(keyword));
                break;
            case "storeLocation":
                condition = condition.and(ro.store.location.containsIgnoreCase(keyword));
                break;
            case "all":
            default:
                condition = condition.and(
                        ro.orderCode.containsIgnoreCase(keyword)
                                .or(ro.store.name.containsIgnoreCase(keyword))
                                .or(ro.store.location.containsIgnoreCase(keyword))
                );
                break;
        }

        return condition;
    }

    // 리스트 개수 카운팅
    @Override
    public long countReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;

        BooleanExpression hasDetails = JPAExpressions
                .selectOne()
                .from(rod)
                .where(rod.receiveOrder.eq(ro))
                .exists();

        Long total = queryFactory
                .select(ro.count())
                .from(ro)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro),
                        hasDetails
                )
                .fetchOne();

        return total != null ? total : 0L;
    }

    // 수주 상세 조회
    @Override
    public Optional<ReceiveOrderDetailDTO> findDetailById(Long id) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QStore store = QStore.store;

        ReceiveOrderDetailDTO dto = queryFactory
                .select(Projections.fields(ReceiveOrderDetailDTO.class,
                        ro.id,
                        ro.orderCode,
                        ro.orderDate,
                        ro.actualDeliveryDate,
                        ro.status,
                        ro.priority,
                        store.name.as("storeName"),
                        store.id.as("storeId"),
                        store.location.as("storeLocation"),
                        ro.totalCount.as("totalCount"),
                        ro.totalPrice,
                        ro.remark
                ))
                .from(ro)
                .leftJoin(ro.store, store)
                .where(ro.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    // 수주 상세 - 주문 상품 리스트
    @Override
    public List<ReceiveOrderItemDTO> findItemsByOrderId(Long id) {
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
        QMaterial material = QMaterial.material;
        QInventory hq = QInventory.inventory;

        return queryFactory
                .selectDistinct(Projections.fields(ReceiveOrderItemDTO.class,
                        material.id.as("materialId"),              // ★ 추가
                        material.name.as("name"),
                        material.materialCategory.as("materialCategory"),
                        rod.count.as("detailCount"),
                        rod.unitPrice.as("detailUnitPrice"),
                        rod.totalPrice.as("detailTotalPrice"),
                        hq.status.as("inventoryStatus")
                ))
                .from(rod)
                .leftJoin(rod.material, material)
                .leftJoin(rod.inventory, hq)
                .where(rod.receiveOrder.id.eq(id))
                .fetch();
    }



}

