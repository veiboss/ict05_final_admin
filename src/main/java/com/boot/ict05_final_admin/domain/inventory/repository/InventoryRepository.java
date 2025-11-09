package com.boot.ict05_final_admin.domain.inventory.repository;


import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 본사 재고(HqInventory) Repository
 *
 * <p>본사 재고의 수량 및 적정 수량 관리 기능을 담당한다.</p>
 */
public interface InventoryRepository
        extends JpaRepository<HqInventory, Long>, InventoryRepositoryCustom {

}