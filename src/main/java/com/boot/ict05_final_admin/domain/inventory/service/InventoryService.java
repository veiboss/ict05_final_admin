package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;

import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

/**
 * 기본 재고 서비스 (InventoryService)
 *
 * <p>간단한 오버뷰/합계 등 서비스 파사드.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository batchRepo;
    private final InventoryLogViewRepository logViewRepo;

    /**
     * 재고 목록을 페이지 단위로 조회한다.
     * HQ 현재고(재료ID 기준)
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
    @Transactional(readOnly = true)
    public List<HqInventory> findAllForSelect() {
        return inventoryRepository.findAll();
    }

    /**
     * HQ 현재고(재료ID 기준)
     *
     * <p>HQ 배치 잔량 합.</p>
     */
    @Comment("HQ 현재고 합계")
    public BigDecimal hqRemainOfMaterial(Long materialId) {
        return batchRepo.findHqBatchesForMaterial(materialId).stream()
                .map(b -> b.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadExcel(Long materialId,
                                                  String type,
                                                  LocalDateTime from,
                                                  LocalDateTime to,
                                                  Pageable pageable) {

        byte[] bytes = buildCsv(materialId, type, from, to, pageable);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"inventory_logs.csv\"")
                .body(new ByteArrayResource(bytes));
    }

    /** CSV 바이트 생성 */
    private byte[] buildCsv(Long materialId,
                            String type,
                            LocalDateTime from,
                            LocalDateTime to,
                            Pageable pageable) {

        var page = logViewRepo.findLogsByFilter(materialId, type,
                from != null ? from.toLocalDate() : null,
                to   != null ? to.toLocalDate()   : null,
                pageable);

        StringJoiner sj = new StringJoiner("\n");
        sj.add("log_id,material_id,type,date,qty,unit_price,memo,store_id");

        page.getContent().forEach(v -> {
            sj.add(String.join(",",
                    String.valueOf(v.getId()),
                    String.valueOf(v.getMaterialId()),
                    safe(v.getType()),
                    safe(v.getDate() != null ? v.getDate().toString() : ""),
                    safe(v.getQuantity() != null ? v.getQuantity().toPlainString() : ""),
                    safe(v.getUnitPrice() != null ? v.getUnitPrice().toPlainString() : ""),
                    csvEscape(v.getMemo()),
                    String.valueOf(v.getStoreId())
            ));
        });

        return sj.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n");
        String t = s.replace("\"", "\"\"");
        return needQuote ? "\"" + t + "\"" : t;
    }
}
