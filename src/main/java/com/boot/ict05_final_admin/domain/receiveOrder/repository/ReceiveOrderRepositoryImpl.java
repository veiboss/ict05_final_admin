package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.QHqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderItemDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrderDetail;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReceiveOrderRepositoryImpl implements ReceiveOrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QStore store = QStore.store;

        // 데이터 목록 조회
        List<ReceiveOrderListDTO> content = queryFactory
                .select(Projections.fields(ReceiveOrderListDTO.class,
                        ro.id,
                        ro.store.name.as("storeName"),
                        ro.store.location.as("storeLocation"),
                        ro.orderCode,
                        ro.orderDate,
                        ro.status,
                        ro.totalPrice,
                        ro.priority,
                        ro.remark,
                        ro.supplier,
                        ro.deliveryDate,
                        ro.actualDeliveryDate
                ))
                .from(ro)
                .join(ro.store, store)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro)
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
                        eqOrderCode(receiveOrderSearchDTO, ro)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    // 검색 필터 - 가맹점명, 주문번호, 지역
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

    // 리스트 개수 카운팅
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

    // 수주 상세 조회
    @Override
    public Optional<ReceiveOrderDetailDTO> findDetailById(Long id) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
        QStore store = QStore.store;

        ReceiveOrderDetailDTO dto = queryFactory
                .select(Projections.fields(ReceiveOrderDetailDTO.class,
                        ro.id,
                        ro.orderCode,
                        ro.orderDate,
                        ro.deliveryDate,
                        ro.status,
                        ro.priority,
                        store.name.as("storeName"),
                        store.id.as("storeId"),
                        store.location.as("storeLocation"),
                        ro.totalPrice,
                        ro.totalCount,
                        ro.remark
                ))
                .from(ro)
                .leftJoin(ro.store, store)
                .leftJoin(ro.details, rod)
                .where(ro.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    // 수주 상세 - 주문 상품 리스트
    @Override
    public List<ReceiveOrderItemDTO> findItemsByOrderId(Long id) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
        QMaterial material = QMaterial.material;
        QHqInventory hq = QHqInventory.hqInventory;

        return queryFactory
                .select(Projections.fields(ReceiveOrderItemDTO.class,
                        material.name,
                        material.materialCategory,
                        rod.detailCount,
                        rod.detailUnitPrice,
                        rod.detailTotalPrice,
                        hq.status.as("inventoryStatus")
                ))
                .from(rod)
                .join(rod.material, material)
                .join(rod.receiveOrder, ro)
                .leftJoin(hq).on(hq.material.eq(material))
                .where(rod.receiveOrder.id.eq(id))
                .fetch();
    }


}

