package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;



@SpringBootTest
class InventoryInServiceTest {

    @Autowired
    private InventoryInService inventoryInService;
    @Autowired
    private MaterialRepository materialRepository;

    /**
     * 적정재고량(>0)인 모든 재료에 대해:
     * - 입고수량 = 적정재고량
     * - 입고가 = 5,000~15,000 (재료ID로 시드 고정)
     * - 판매가 = 입고가*1.1을 10원 단위 반올림
     * - 입고시각 = now
     * - 메모 = SEED
     *
     * 서비스 경유 → 재고수량, 상태, updateDate, 배치, stockAfter 자동 반영
     */
//    @Test
//    @Transactional
//    @Rollback(false)
//    void seedInitialStockByOptimalQuantity() {
//        materialRepository.findAll().stream()
//                .filter(m -> m.getOptimalQuantity() != null && m.getOptimalQuantity().signum() > 0)
//                .forEach(m -> {
//                    final long materialId = m.getId();
//                    final BigDecimal qty = m.getOptimalQuantity().setScale(3, RoundingMode.HALF_UP);
//
//                    // 입고가: 5,000~15,000, 10원 단위, ID 기반 고정 난수
//                    final int unitPrice = 5000 + (new Random(materialId).nextInt(1001) * 10);
//
//                    // 판매가: 입고가*1.1 → 10원 단위 반올림
//                    final int sellingPrice = Math.round(unitPrice * 1.1f / 10f) * 10;
//
//                    InventoryInWriteDTO dto = new InventoryInWriteDTO();
//                    dto.setMaterialId(materialId);
//                    dto.setQuantity(qty);
//                    dto.setUnitPrice(BigDecimal.valueOf(unitPrice));
//                    dto.setSellingPrice(BigDecimal.valueOf(sellingPrice));
//                    dto.setInDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
//                    dto.setMemo("SEED: optimal as initial stock");
//
//                    inventoryInService.insertInventoryIn(dto);
//                });
//    }

}