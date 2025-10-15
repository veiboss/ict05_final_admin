package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiveOrderRepository extends JpaRepository<ReceiveOrder, Long>, ReceiveOrderRepositoryCustom {
}
