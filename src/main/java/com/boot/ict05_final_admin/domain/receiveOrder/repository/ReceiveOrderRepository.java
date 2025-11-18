package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReceiveOrderRepository extends JpaRepository<ReceiveOrder, Long>, ReceiveOrderRepositoryCustom {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE receive_order
       SET purchase_order_status = :next
     WHERE purchase_order_id = :id
       AND purchase_order_status = :current
    """, nativeQuery = true)
        int updateStatusIfCurrent(@Param("id") Long id,
                                  @Param("current") String current,
                                  @Param("next") String next);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE receive_order
       SET purchase_order_status = :status
     WHERE purchase_order_code = :orderCode
    """, nativeQuery = true)
        int updateStatusByOrderCode(@Param("orderCode") String orderCode,
                                    @Param("status") String status);
}
