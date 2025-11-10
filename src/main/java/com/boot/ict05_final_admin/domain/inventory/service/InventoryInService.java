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
 * <p>입고 저장과 배치 생성만 담당한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryInService {

    private final EntityManager em;
    private final InventoryInRepository inRepo;
    private final InventoryBatchRepository batchRepo;

    /** 표시 규칙과 일치: 소수점 3자리 Half-Up 고정 */
    private static BigDecimal s3(BigDecimal v) {
        return v == null ? null : v.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * 본사 입고 등록.
     *
     * <p>요구사항에 맞춰
     * receivedDate=LocalDateTime, expirationDate=LocalDate,
     * qty/unitPrice/sellingPrice=소수점 3자리로 저장한다.</p>
     *
     * @param dto 입고 등록 DTO
     * @return 생성된 입고 ID
     */
    @Transactional
    public Long insertInventoryIn(InventoryInWriteDTO dto) {
        if (dto.getMaterialId() == null) throw new IllegalArgumentException("materialId is required");
        if (dto.getQuantity() == null || dto.getQuantity().signum() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (dto.getUnitPrice() == null || dto.getUnitPrice().signum() < 0) throw new IllegalArgumentException("unitPrice must be >= 0");

        final Material ref = em.getReference(Material.class, dto.getMaterialId());
        final BigDecimal qty = s3(dto.getQuantity());
        final BigDecimal unitPrice = s3(dto.getUnitPrice());
        final BigDecimal sellingPrice = s3(dto.getSellingPrice());
        final LocalDateTime inDate = dto.getInDate() != null ? dto.getInDate() : LocalDateTime.now();
        final LocalDate expirationDate = dto.getExpirationDate();

        // 1) lotNo 생성: 코드-YYYYMMDD-#### (예: ETC0005-20251110-0123)
        final String lotNo = generateLotNo();

        // 2) 입고 저장 (+ lotNo 반영)
        InventoryIn in = InventoryIn.builder()
                .material(ref)
                .inDate(inDate)
                .quantity(qty)
                .unitPrice(unitPrice)
                .sellingPrice(sellingPrice)
                .memo(dto.getMemo())
                .lotNo(lotNo)
                .build();
        try {
            in.getClass().getMethod("setLotNo", String.class).invoke(in, lotNo); // 세터가 있을 때
        } catch (Exception ignore) { /* 엔티티에 lotNo 없으면 무시 */ }
        in = inRepo.save(in);

        // 3) 배치 저장 (동일 lotNo)
        InventoryBatch b = InventoryBatch.builder()
                .material(ref)
                .store(null)
                .receivedDate(inDate)
                .expirationDate(expirationDate)
                .receivedQuantity(qty)
                .quantity(qty)
                .unitPrice(unitPrice)
                .lotNo(lotNo)
                .build();
        batchRepo.save(b);

        return in.getId();
    }

    // 본사 입고
    /** 기존 다중 파라미터는 DTO 버전으로 위임. 점진 폐기. */
    @Deprecated
    @Transactional
    public Long receiveToHq(Long materialId,
                            BigDecimal unitPrice,
                            BigDecimal sellingPrice,
                            LocalDateTime inDate,
                            LocalDateTime expirationDateTime, // 과거 시그니처 유지용
                            String memo) {

        InventoryInWriteDTO dto = new InventoryInWriteDTO();
        dto.setMaterialId(materialId);
        dto.setQuantity(BigDecimal.ZERO); // 호출부가 수량을 몰랐다면 명시적으로 세팅 필요
        dto.setUnitPrice(unitPrice);
        dto.setSellingPrice(sellingPrice);
        dto.setInDate(inDate);
        dto.setExpirationDate(expirationDateTime != null ? expirationDateTime.toLocalDate() : null);
        dto.setMemo(memo);
        return insertInventoryIn(dto);
    }

    // 삭제
    @Transactional
    public void deleteIn(Long inId) {
        inRepo.deleteById(inId);
    }

    // 포맷: CODE-YYYYMMDD-####
    private String generateLotNo() {
        final java.time.LocalDate today = java.time.LocalDate.now();
        final String ymd = String.format("%02d%02d%02d",
                today.getYear() % 100, today.getMonthValue(), today.getDayOfMonth());

        for (int i = 0; i < 5; i++) { // 최대 5회 재시도
            int seq = (int) (System.nanoTime() % 1000);
            String lot = "LOT" + ymd + "-" + String.format("%03d", seq);
            if (!inRepo.existsByLotNo(lot) && !batchRepo.existsByLotNo(lot)) {
                return lot;
            }
        }
        // 드물게 모두 충돌 시 타임스탬프 가미
        String fallback = "LOT" + ymd + "-" + (System.currentTimeMillis() % 1000);
        return String.format("LOT%s-%03d", ymd, Integer.parseInt(fallback.substring(fallback.length()-3)));
    }
}
