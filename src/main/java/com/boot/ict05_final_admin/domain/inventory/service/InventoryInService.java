package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 입고 서비스
 *
 * <p>본사 재고 입고, 재고 수량 갱신, 배치 생성을 담당한다.</p>
 * <p>표시 규칙: qty/unitPrice/sellingPrice는 소수점 3자리 Half-Up.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryInService {

    private final EntityManager em;
    private final InventoryInRepository inRepo;
    private final InventoryBatchRepository batchRepo;
    private final InventoryStockService stockService; // ← 재고 증감/상태 갱신 전담 서비스

    /** 소수점 3자리 Half-Up 고정 */
    private static BigDecimal s3(BigDecimal v) {
        return v == null ? null : v.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * 본사 입고 등록.
     *
     * <p>트랜잭션 내 순서</p>
     * <ol>
     *   <li>파라미터 정규화 및 참조 확보</li>
     *   <li>본사 재고 수량 += qty (비관 잠금은 {@link InventoryStockService} 내 처리)</li>
     *   <li>입고 로그 저장( stockAfter = after )</li>
     *   <li>동일 lotNo로 배치 생성</li>
     * </ol>
     *
     * @param dto 입고 등록 DTO
     * @return 생성된 입고 ID
     */
    @Transactional
    public Long insertInventoryIn(InventoryInWriteDTO dto) {
        // 0) 검증
        if (dto.getMaterialId() == null) throw new IllegalArgumentException("materialId is required");
        if (dto.getQuantity() == null || dto.getQuantity().signum() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (dto.getUnitPrice() == null || dto.getUnitPrice().signum() < 0) throw new IllegalArgumentException("unitPrice must be >= 0");

        // 1) 정규화
        final Material materialRef = em.getReference(Material.class, dto.getMaterialId());
        final BigDecimal qty = s3(dto.getQuantity());
        final BigDecimal unitPrice = s3(dto.getUnitPrice());
        final BigDecimal sellingPrice = s3(dto.getSellingPrice());
        final LocalDateTime inDate = dto.getInDate() != null ? dto.getInDate() : LocalDateTime.now();

        // 2) 재고 갱신: 본사 재고 += qty → after 반환
        final BigDecimal after = stockService.addToInventory(dto.getMaterialId(), qty); // 내부에서 touchAfterQuantityChange 호출됨

        // 3) lotNo 생성
        final String lotNo = generateLotNo();

        // 4) 입고 로그 저장 (stockAfter = after)
        InventoryIn in = InventoryIn.builder()
                .material(materialRef)
                .inDate(inDate)
                .quantity(qty)
                .unitPrice(unitPrice)
                .sellingPrice(sellingPrice)
                .memo(dto.getMemo())
                .lotNo(lotNo)
                .stockAfter(after) // ← 필수
                .build();
        in = inRepo.save(in);

        // 5) 배치 저장 (동일 lotNo)
        InventoryBatch batch = InventoryBatch.builder()
                .material(materialRef)
                .store(null) // 본사 배치
                .receivedDate(inDate)
                .expirationDate(dto.getExpirationDate())
                .receivedQuantity(qty)
                .quantity(qty) // 현재 잔량 = 최초 입고수량
                .unitPrice(unitPrice)
                .lotNo(lotNo)
                .build();
        batchRepo.save(batch);

        return in.getId();
    }

    /**
     * 구 시그니처 호환. 점진 폐기 예정.
     */
    @Deprecated
    @Transactional
    public Long receiveToHq(Long materialId,
                            BigDecimal unitPrice,
                            BigDecimal sellingPrice,
                            LocalDateTime inDate,
                            LocalDateTime expirationDateTime,
                            String memo) {

        InventoryInWriteDTO dto = new InventoryInWriteDTO();
        dto.setMaterialId(materialId);
        dto.setQuantity(BigDecimal.ZERO); // 호출부가 수량을 모른다면 명시적으로 세팅 필요
        dto.setUnitPrice(unitPrice);
        dto.setSellingPrice(sellingPrice);
        dto.setInDate(inDate);
        dto.setExpirationDate(expirationDateTime != null ? expirationDateTime.toLocalDate() : null);
        dto.setMemo(memo);
        return insertInventoryIn(dto);
    }

    /** 입고 삭제. 배치/재고 롤백은 별도 정책에 따름. */
    @Transactional
    public void deleteIn(Long inId) {
        inRepo.deleteById(inId);
    }

    /** 포맷: LOT-YYMMDD-######, 충돌 시 재시도 */
    private String generateLotNo() {
        final LocalDate today = LocalDate.now();
        final String ymd = String.format("%02d%02d%02d",
                today.getYear() % 100, today.getMonthValue(), today.getDayOfMonth());

        // 충돌 가능성 낮추기 위해 6자리 시퀀스 사용
        for (int i = 0; i < 5; i++) {
            String seq = String.format("%06d", Math.floorMod(System.nanoTime(), 1_000_000));
            String lot = "LOT-" + ymd + "-" + seq;
            if (!inRepo.existsByLotNo(lot) && !batchRepo.existsByLotNo(lot)) return lot;
        }
        return "LOT-" + ymd + "-" + String.format("%06d", Math.floorMod(System.currentTimeMillis(), 1_000_000));
    }
}