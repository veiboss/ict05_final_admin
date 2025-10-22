package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderItemDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
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

}
