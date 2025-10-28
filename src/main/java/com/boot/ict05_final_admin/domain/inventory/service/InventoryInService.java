package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryInRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * 본사 재고 입고 서비스
 *
 * <p>1. 입고 이력 저장 (InventoryIn)</p>
 * <p>2. 본사 재고 수량 증가 (Inventory)</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryInService {

    private final InventoryInRepository inventoryInRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * 본사 입고 등록
     */
    public Long insertInventoryIn(InventoryInWriteDTO dto) {
        HqInventory inventory = (HqInventory) inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재고를 찾을 수 없습니다."));

        Material material = inventory.getMaterial();

        // 입고 엔티티 생성
        InventoryIn entity = InventoryIn.builder()
                .material(material)
                .store(null)
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .sellingPrice(dto.getSellingPrice())
                .inDate(dto.getInDate())
                .memo(dto.getMemo())
                .build();

        inventoryInRepository.save(entity);

        // 재고 수량 증가
        inventoryRepository.addQuantity(material.getId(), dto.getQuantity());

        // 상태 갱신
        inventory.updateStatus();

        return entity.getId();
    }
}
