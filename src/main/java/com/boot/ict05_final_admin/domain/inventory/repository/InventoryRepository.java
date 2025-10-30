package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 본사 재고(HqInventory) Repository
 *
 * <p>본사 재고의 수량 및 적정 수량 관리 기능을 담당한다.</p>
 */
public interface InventoryRepository
        extends JpaRepository<HqInventory, Long>, InventoryRepositoryCustom {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE HqInventory i SET i.optimalQuantity = :optimalQty WHERE i.material.id = :materialId")
    void updateOptimalQuantityByMaterialId(@Param("materialId") Long materialId,
                                           @Param("optimalQty") BigDecimal optimalQty);
    /**
     * 재료 ID로 재고 조회
     */
    @Query("SELECT i FROM HqInventory i WHERE i.material.id = :materialId")
    Optional<HqInventory> findByMaterialId(@Param("materialId") Long materialId);

    /**
     * 재료 엔티티로 재고 조회
     */
    Optional<HqInventory> findByMaterial(Material material);

    /**
     * 입고 시 수량 증가
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE HqInventory i SET i.quantity = i.quantity + :quantity WHERE i.material.id = :materialId")
    void addQuantity(@Param("materialId") Long materialId, @Param("quantity") BigDecimal quantity);

    /**
     * 출고 시 수량 감소
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE HqInventory i SET i.quantity = i.quantity - :quantity WHERE i.material.id = :materialId")
    void subtractQuantity(@Param("materialId") Long materialId, @Param("quantity") BigDecimal quantity);
}
