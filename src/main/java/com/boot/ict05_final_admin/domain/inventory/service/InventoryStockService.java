package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InventoryStockService {

    private final InventoryRepository inventoryRepository;
    private final EntityManager em;

    /**
     * 본사 재고 증가: 없으면 행을 생성하고 0에서 시작해 더한다.
     * 상태와 updateDate는 엔티티 메서드로 동기화된다.
     *
     * @return 증가 후 수량(after)
     */
    @Transactional
    public BigDecimal addToInventory(Long materialId, BigDecimal delta) {
        if (delta == null || delta.signum() <= 0)
            throw new IllegalArgumentException("delta must be > 0");

        // PESSIMISTIC_WRITE로 행 잠금. 없으면 생성.
        Inventory inv = inventoryRepository.findByMaterialIdForUpdate(materialId)
                .orElseGet(() -> {
                    Material mRef = em.getReference(Material.class, materialId);
                    Inventory created = Inventory.builder()
                            .material(mRef)
                            .quantity(BigDecimal.ZERO)
                            // 초기 적정수량을 재료값으로 동기화하고 싶다면 다음 줄 유지
                            .optimalQuantity(mRef.getOptimalQuantity())
                            .build();
                    return inventoryRepository.saveAndFlush(created); // 즉시 생성 후 잠금 컨텍스트에 올림
                });

        BigDecimal before = inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity();
        BigDecimal after = before.add(delta);
        inv.setQuantity(after);
        inv.touchAfterQuantityChange(); // 상태 + updateDate 동기화
        // JPA flush는 호출자 트랜잭션 종료 시점
        return after;
    }
}
