package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 배치(로트) 저장소
 *
 * <p>조회는 QueryDSL 구현(Impl) 사용.</p>
 */
@Repository
public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long>, InventoryBatchRepositoryCustom {
    boolean existsByLotNo(String lotNo);   // InventoryBatch.lotNo 매핑(= inventory_batch_lot_no)
}
