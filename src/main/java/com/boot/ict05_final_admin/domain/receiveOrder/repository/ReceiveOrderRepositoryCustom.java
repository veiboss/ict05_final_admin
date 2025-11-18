package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.*;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReceiveOrderRepositoryCustom {

    // 수주 목록 조회
    Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable);
    // 수주 총 개수
    long countReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO);
    // 수주 상세 조회
    Optional<ReceiveOrderDetailDTO> findDetailById(Long id);
    // 수주 상세 - 주문 상품 리스트
    List<ReceiveOrderItemDTO> findItemsByOrderId(Long id);
    // 상단 카드 데이터
    ReceiveOrderSummaryDTO getSummary();
    // 수주 코드로 상태 변경 시에도 필요
    Optional<ReceiveOrder> findOrderById(Long id);
}
