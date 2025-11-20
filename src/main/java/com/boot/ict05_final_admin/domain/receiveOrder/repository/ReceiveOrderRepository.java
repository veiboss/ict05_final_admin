package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReceiveOrderRepository extends JpaRepository<ReceiveOrder, Long>, ReceiveOrderRepositoryCustom {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReceiveOrder ro " +
            "SET ro.status = :next " +
            "WHERE ro.id = :id AND ro.status = :curr")
    int updateStatusIfCurrent(@Param("id") Long id,
                              @Param("curr") ReceiveOrderStatus curr,
                              @Param("next") ReceiveOrderStatus next);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReceiveOrder ro " +
            "SET ro.status = :status " +
            "WHERE ro.orderCode = :orderCode")
    int updateStatusByOrderCode(@Param("orderCode") String orderCode,
                                @Param("status") ReceiveOrderStatus status);
}
