package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 배치(로트) 저장소
 *
 * <p>조회는 QueryDSL 구현(Impl) 사용.</p>
 */
@Repository
public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long>, InventoryBatchRepositoryCustom {

    // 단순 메서드: 재료ID로 정렬 조회
    List<InventoryBatch> findAllByMaterial_IdOrderByReceivedDateDesc(Long materialId);

    // InventoryBatch.lotNo 매핑(= inventory_batch_lot_no)
    boolean existsByLotNo(String lotNo);

}
