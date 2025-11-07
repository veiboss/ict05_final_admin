package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryInRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입고 서비스 (InventoryInService)
 *
 * <p>HQ 입고를 처리하고 배치를 생성한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryInService {

    private final InventoryInRepository inRepo;
    private final InventoryBatchRepository batchRepo;

    @PersistenceContext
    private EntityManager em;


    /**
     * HQ 입고 처리 + 배치 생성
     *
     * @param materialId 재료 ID
     * @param qty 수량
     * @param unitPrice 단가
     * @param receivedDate 입고일시
     * @param expirationDate 유통기한(옵션)
     * @param memo 메모
     * @return 입고 ID
     */
    @Transactional
    @Comment("HQ 입고 → 배치 생성")
    public Long receiveToHq(Long materialId,
                            BigDecimal qty,
                            BigDecimal unitPrice,
                            LocalDateTime receivedDate,
                            LocalDateTime expirationDate,
                            String memo) {

        InventoryIn in = InventoryIn.builder()
                .material(em.getReference(Material.class, materialId))
                .inDate(receivedDate != null ? receivedDate : LocalDateTime.now())
                .quantity(qty)
                .unitPrice(unitPrice)
                .memo(memo)
                .build();
        in = inRepo.save(in);

        InventoryBatch b = InventoryBatch.builder()
                .material(em.getReference(Material.class, materialId))
                .store(null) // HQ
                .receivedDate(in.getInDate())
                // 엔티티가 LocalDate인 경우만 아래처럼 변환
                .expirationDate(expirationDate != null ? expirationDate.toLocalDate() : null)
                .receivedQuantity(qty)
                .quantity(qty)
                .unitPrice(unitPrice)
                .lotNo(generateLotNo(materialId, in.getInDate()))
                .build();
        batchRepo.save(b);

        return in.getId();
    }


    // 로트번호 생성 헬퍼
    private String generateLotNo(Long materialId, LocalDateTime receivedAt) {
        String day = receivedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        long seq = batchRepo.countByMaterialAndDate(materialId, receivedAt.toLocalDate()) + 1;
        return String.format("LOT%s-%03d", day, seq);
    }
}
