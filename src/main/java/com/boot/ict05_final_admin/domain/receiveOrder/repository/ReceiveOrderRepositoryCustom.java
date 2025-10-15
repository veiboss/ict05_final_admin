package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

public interface ReceiveOrderRepositoryCustom {
    Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable);
    long countReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO);

}
