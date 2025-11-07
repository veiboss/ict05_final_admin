package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 출고-로트 아이템 저장소
 *
 * <p>조회는 QueryDSL 구현(Impl) 사용.</p>
 */
@Repository
public interface InventoryOutLotRepository
        extends JpaRepository<InventoryOutLot, Long>, InventoryOutLotRepositoryCustom {
}
