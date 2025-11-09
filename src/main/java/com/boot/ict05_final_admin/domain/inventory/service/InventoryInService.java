package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryInRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입고 서비스
 *
 * <p>입고 저장과 배치 생성만 담당한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryInService {

    private final EntityManager em;
    private final InventoryInRepository inRepo;
    private final InventoryBatchRepository batchRepo;

    // 본사 입고
    @Transactional
    public Long receiveToHq(Long materialId,
                            BigDecimal unitPrice,
                            BigDecimal sellingPrice,   // null 허용
                            LocalDateTime inDate,
                            LocalDateTime expirationDate, // null 허용
                            String memo) {

        InventoryIn in = InventoryIn.builder()
                .material(em.getReference(Material.class, materialId))
                .inDate(inDate != null ? inDate : LocalDateTime.now())
                .quantity(BigDecimal.ZERO) // 호출부에서 실제 수량을 주입한다면 교체
                .unitPrice(unitPrice)
                .sellingPrice(sellingPrice)
                .memo(memo)
                .build();
        in = inRepo.save(in);

        InventoryBatch b = InventoryBatch.builder()
                .material(em.getReference(Material.class, materialId))
                .store(null)
                .receivedDate(in.getInDate())
                .expirationDate(expirationDate != null ? expirationDate.toLocalDate() : null)
                .receivedQuantity(in.getQuantity())
                .quantity(in.getQuantity())
                .unitPrice(unitPrice)
                .lotNo(generateLotNo())
                .build();
        batchRepo.save(b);

        return in.getId();
    }

    // 삭제
    @Transactional
    public void deleteIn(Long inId) {
        inRepo.deleteById(inId);
    }

    /** LOTyyMMdd-### */
    private String generateLotNo() {
        var today = java.time.LocalDate.now();
        String ymd = String.format("%02d%02d%02d", today.getYear() % 100, today.getMonthValue(), today.getDayOfMonth());
        int seq = (int) (System.nanoTime() % 1000);
        return "LOT" + ymd + "-" + String.format("%03d", seq);
    }
}
