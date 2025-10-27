package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface StoreInventoryRepository
        extends JpaRepository<StoreInventory, Long>, StoreInventoryRepositoryCustom {

    /** 가맹점 재료 ID로 Inventory 조회 */
    @Query("SELECT si FROM StoreInventory si WHERE si.storeMaterial.id = :storeMaterialId")
    Optional<StoreInventory> findByStoreMaterialId(@Param("storeMaterialId") Long storeMaterialId);

    /** 입고 시 수량 증가 */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StoreInventory si SET si.quantity = si.quantity + :quantity WHERE si.storeMaterial.id = :storeMaterialId")
    void addQuantity(@Param("storeMaterialId") Long storeMaterialId, @Param("quantity") BigDecimal quantity);

    /** 출고 시 수량 감소 */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StoreInventory si SET si.quantity = si.quantity - :quantity WHERE si.storeMaterial.id = :storeMaterialId")
    void subtractQuantity(@Param("storeMaterialId") Long storeMaterialId, @Param("quantity") BigDecimal quantity);
}