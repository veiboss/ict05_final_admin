package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPriceType;
import com.boot.ict05_final_admin.domain.inventory.repository.UnitPriceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitPriceService {

    private final UnitPriceRepository unitPriceRepository;

    @PersistenceContext
    private EntityManager em;

    /** 기준 시각 최신 매입가. 없으면 null */
    public BigDecimal getLatestPurchasePrice(Long materialId, LocalDateTime at) {
        return unitPriceRepository.findLatestPurchasePrice(materialId, at)
                .map(UnitPrice::getPurchasePrice)     // ← 엔티티 필드명에 맞춤
                .orElse(null);
    }

    /** 매입가 신규 등록. (중복·정합성 검증은 네 정책에 맞게 확장) */
    @Transactional
    public Long setPurchasePrice(Long materialId, BigDecimal price, LocalDateTime validFrom, String memo) {
        if (materialId == null) throw new IllegalArgumentException("materialId 필수");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("단가는 0 이상");
        if (validFrom == null) throw new IllegalArgumentException("validFrom 필수");

        UnitPrice row = UnitPrice.builder()
                .material(em.getReference(Material.class, materialId))
                .type(UnitPriceType.PURCHASE)
                .purchasePrice(price)
                .validFrom(validFrom)
                .build();

        return unitPriceRepository.save(row).getId();
    }

    /** 최근 매입가 이력 N건 */
    public List<UnitPrice> historyPurchasePrice(Long materialId, int limit) {
        return unitPriceRepository.historyPurchasePrice(materialId, limit);
    }
}
