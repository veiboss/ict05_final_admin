package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * 본사 입출고 통합 로그 Repository
 */
@Repository
public interface InventoryLogViewRepository extends JpaRepository<InventoryLogView, Long>, InventoryLogViewRepositoryCustom {
}