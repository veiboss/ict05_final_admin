package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 본사 재고 서비스
 *
 * <p>재고 목록 조회를 담당한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 목록을 페이지 단위로 조회한다.
     *
     * @param inventorySearchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 페이징 처리된 재료 리스트
     */
    @Transactional(readOnly = true)
    public Page<InventoryListDTO> getInventoryList(InventorySearchDTO inventorySearchDTO, Pageable pageable) {
        return inventoryRepository.listInventory(inventorySearchDTO, pageable);
    }


    /**
     * 본사 입고 등록용 - 재고 선택 목록 조회
     * (재고 + 재료명 출력용)
     */
    public List<HqInventory> findAllForSelect() {
        return inventoryRepository.findAll();
    }

    /**
     * 본사 재고 단건 조회
     *
     * <p>재고 ID를 기준으로 본사 재고 정보를 조회한다.</p>
     *
     * @param id 본사 재고 ID
     * @return HqInventory 엔티티
     */
    @Transactional(readOnly = true)
    public HqInventory findById(Long id) {
        HqInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 본사 재고가 존재하지 않습니다. ID=" + id));

        // === 상태 재계산 (단순화) ===
        if (inventory.getMaterial() != null) {
            inventory.setStatus(
                    InventoryStatus.calculate(inventory.getQuantity(), inventory.getMaterial().getOptimalQuantity())
            );
        }

        return inventory;
    }
}
