package com.boot.ict05_final_admin.domain.receiveOrder.service;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.repository.ReceiveOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

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


}
