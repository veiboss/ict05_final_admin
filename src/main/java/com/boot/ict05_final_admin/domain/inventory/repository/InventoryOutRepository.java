package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 출고 헤더 저장소
 *
 * <p>조회는 QueryDSL 구현(Impl) 사용.</p>
 */
@Repository
public interface InventoryOutRepository
        extends JpaRepository<InventoryOut, Long>, InventoryOutRepositoryCustom {
}
