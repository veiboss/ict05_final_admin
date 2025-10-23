package com.boot.ict05_final_admin.domain.receiveOrder.service;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderItemDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import com.boot.ict05_final_admin.domain.receiveOrder.repository.ReceiveOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ReceiveOrderService {

    private final ReceiveOrderRepository receiveOrderRepository;

    /**
     * 수주 목록을 페이지 단위로 조회한다.
     *
     * @param receiveOrderSearchDTO  (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 수주 리스트 DTO
     */
    public Page<ReceiveOrderListDTO> selectAllOfficeReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        return receiveOrderRepository.listReceive(receiveOrderSearchDTO, pageable);
    }

    /* 주문 상세 정보 조회 */
    public ReceiveOrderDetailDTO getReceiveOrderDetail(Long id) {
        ReceiveOrderDetailDTO dto = receiveOrderRepository.findDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("수주 내역이 존재하지 않습니다. id=" + id));

        List<ReceiveOrderItemDTO> items = receiveOrderRepository.findItemsByOrderId(id);
        dto.setItems(items);  // setter로 주입

        return dto;
    }

    /* 배송 상태 변경 */
    @Transactional
    public void advanceStatus(Long id) {
        ReceiveOrder order = receiveOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다. id=" + id));

        ReceiveOrderStatus current = order.getStatus();
        ReceiveOrderStatus next;

        switch (current) {
            case RECEIVED -> next = ReceiveOrderStatus.PREPARING;
            case PREPARING -> next = ReceiveOrderStatus.SHIPPING;
            case SHIPPING -> next = ReceiveOrderStatus.DELIVERED;
            default -> throw new IllegalStateException("배송 완료된 주문은 변경할 수 없습니다.");
        }

        order.setStatus(next);
        receiveOrderRepository.save(order);
    }



}
