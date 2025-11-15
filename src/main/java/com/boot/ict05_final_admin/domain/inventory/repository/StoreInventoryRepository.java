package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreInventoryRepository
        extends JpaRepository<StoreInventory, Long>, StoreInventoryRepositoryCustom {

    /** 가맹점 재료 ID로 InventoryBase 조회 */
    @Query("SELECT si FROM StoreInventory si WHERE si.storeMaterial.id = :storeMaterialId")
    Optional<StoreInventory> findByStoreMaterialId(@Param("storeMaterialId") Long storeMaterialId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
  select si
  from StoreInventory si
  join si.storeMaterial sm
  join sm.material m
  where si.store.id = :storeId
    and sm.isHqMaterial = true
    and m.id = :materialId
""")
    Optional<StoreInventory> findByStoreIdAndMaterialIdForUpdate(Long storeId, Long materialId);


    @Query("""
  select si
  from StoreInventory si
  join si.storeMaterial sm
  join sm.material m
  where si.store.id = :storeId
    and sm.isHqMaterial = true
    and m.id = :materialId
""")
    Optional<StoreInventory> findByStoreIdAndMaterialId(Long storeId, Long materialId);


    @Query("""
  select case when count(si)>0 then true else false end
  from StoreInventory si
  join si.storeMaterial sm
  join sm.material m
  where si.store.id = :storeId
    and sm.isHqMaterial = true
    and m.id = :materialId
""")
    boolean existsByStoreIdAndMaterialId(Long storeId, Long materialId);


    @Query("""
  select si
  from StoreInventory si
  join si.storeMaterial sm
  join sm.material m
  where si.store.id = :storeId
    and sm.isHqMaterial = true
    and (:materialId is null or m.id = :materialId)
""")
    Page<StoreInventory> findAllByStoreIdAndMaterialId(
            Long storeId, Long materialId, Pageable pageable);


}