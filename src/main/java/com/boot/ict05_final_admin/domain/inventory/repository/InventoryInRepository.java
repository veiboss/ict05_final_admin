package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryInRepository
        extends JpaRepository<InventoryIn, Long>, InventoryInRepositoryCustom {
    boolean existsByLotNo(String lotNo);   // lotNo는 엔티티 필드명 기준
}
