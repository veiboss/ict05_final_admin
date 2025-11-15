package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 출고 헤더 저장소
 *
 * <p>조회는 QueryDSL 구현(Impl) 사용.</p>
 */
@Repository
public interface InventoryOutRepository
        extends JpaRepository<InventoryOut, Long>, InventoryOutRepositoryCustom {

    /**
     * 해당 재료의 가장 최근 출고 1건 조회
     * (출고일시 → ID 역순)
     */
    Optional<InventoryOut> findTopByMaterial_IdOrderByOutDateDescIdDesc(Long materialId);
}
