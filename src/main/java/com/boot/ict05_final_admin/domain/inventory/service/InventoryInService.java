package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryInRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 본사 재고 입고 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>입고 이력 등록 및 본사 재고 수량 갱신을 담당한다.</p>
 *
 * <ul>
 *     <li>입고 이력(InventoryIn) 저장</li>
 *     <li>본사 재고(HqInventory) 수량 증가 및 상태 갱신</li>
 * </ul>
 *
 * @author ICT
 * @since 2025.10
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryInService {

    private final InventoryInRepository inventoryInRepository;
    private final InventoryRepository inventoryRepository;
    private final MaterialRepository materialRepository;

    /**
     * 본사 입고 등록
     *
     * <p>선택된 재료(Material)에 대한 입고 정보를 등록하고,
     * 해당 재료의 본사 재고 수량을 증가시킨다.</p>
     *
     * @param dto 입고 등록 정보 (재료 ID, 입고 수량, 단가, 입고일, 비고)
     * @return 생성된 입고 이력(InventoryIn)의 ID
     * @throws IllegalArgumentException 재료 또는 재고가 존재하지 않을 경우 발생
     */
    public Long insertInventoryIn(InventoryInWriteDTO dto) {
        log.info("[입고등록] 요청 수신 - materialId={}, quantity={}, unitPrice={}",
                dto.getMaterialId(), dto.getQuantity(), dto.getUnitPrice());

        // 1. 재료 조회
        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));

        // 2. 본사 재고 조회
        HqInventory inventory = inventoryRepository.findByMaterial(material)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료의 본사 재고 정보가 존재하지 않습니다."));

        // 3. 입고 이력 엔티티 생성 및 저장
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
        log.info("[입고등록] 입고 이력 저장 완료 - id={}", entity.getId());

        // 4. 본사 재고 수량 증가
        inventoryRepository.addQuantity(material.getId(), dto.getQuantity());
        log.info("[입고등록] 본사 재고 수량 증가 완료 - materialId={}, 추가수량={}", material.getId(), dto.getQuantity());

        // 5. 상태 갱신 (충분/부족/품절)
        inventory.updateStatus();
        log.info("[입고등록] 재고 상태 갱신 완료 - materialId={}, status={}", material.getId(), inventory.getStatus());

        return entity.getId();
    }
}