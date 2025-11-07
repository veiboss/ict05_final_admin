package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.OutConfirmRequest;
import com.boot.ict05_final_admin.domain.inventory.dto.OutPreviewItemDTO;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryInService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryOutService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryService;
import com.boot.ict05_final_admin.domain.inventory.service.UnitPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 등록/수정/다운로드 전용 REST 컨트롤러
 */
@RestController
@RequestMapping("/API")
@RequiredArgsConstructor
public class InventoryRestController {

    private final InventoryOutService outService;
    private final InventoryInService inService;
    private final UnitPriceService unitPriceService;
    private final InventoryService inventoryService;

    // -------------------- Out --------------------

    /**
     * 출고 미리보기를 수행한다(FIFO).
     *
     * @param materialId 재료 ID
     * @param qty        총 출고 수량
     * @return 배치 분할 미리보기 결과
     */
    @PostMapping("/inventory/out/preview")
    public List<OutPreviewItemDTO> previewOut(@RequestParam Long materialId,
                                              @RequestParam BigDecimal qty) {
        return outService.previewFifo(materialId, qty);
    }

    /**
     * 출고를 확정한다(배치 할당 포함).
     *
     * @param req 출고 확정 요청 DTO
     * @return 생성된 출고 ID
     */
    @PostMapping("/inventory/out/confirm")
    public Long confirmOut(@RequestBody OutConfirmRequest req) {
        // 서비스가 DTO 오버로드를 제공하지 않으면 5파라미터 시그니처로 위임
        return outService.confirmOut(
                req.getMaterialId(),
                req.getStoreId(),
                req.getTotalQty(),
                req.getOutDate(),
                req.getMemo()
        );
    }

    // -------------------- In --------------------

    /**
     * 본사 입고를 등록한다. 단가가 비어 있으면 최신 매입가로 보충하며 배치를 자동 생성한다.
     *
     * @param dto 입고 등록 DTO
     * @return 생성된 입고 ID
     */
    @PostMapping("/inventory/in")
    public Long receiveToHq(@RequestBody InventoryInWriteDTO dto) {
        return inService.receiveToHq(
                dto.getMaterialId(),
                dto.getUnitPrice(),
                dto.getSellingPrice(),
                dto.getInDate(),
                null,                // expirationDate 없으면 null
                dto.getMemo()
        );
    }

    // -------------------- Unit Price --------------------

    /**
     * 매입가를 등록한다.
     *
     * @param materialId 재료 ID
     * @param price      단가
     * @param validFrom  유효 시작 시각(ISO DATETIME)
     * @return 생성된 단가 ID
     */
    @PostMapping("/unit-price/purchase")
    public Long registerPurchase(@RequestParam Long materialId,
                                 @RequestParam BigDecimal price,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 LocalDateTime validFrom) {
        return unitPriceService.setPurchasePrice(materialId, price, validFrom, null);
    }

    /**
     * 매입가를 수정한다.
     *
     * @param unitPriceId 단가 ID
     * @param price       수정 단가
     * @return 수정된 단가 ID
     */
    @PutMapping("/unit-price/purchase/{unitPriceId}")
    public Long updatePurchase(@PathVariable Long unitPriceId,
                               @RequestParam BigDecimal price) {
        return unitPriceService.updatePurchasePrice(unitPriceId, price);
    }

    // -------------------- Download --------------------

    /**
     * 재고 로그를 엑셀로 다운로드한다.
     *
     * @param materialId 재료 ID
     * @param type       구분(입고/출고/조정), null 가능
     * @param from       시작일시(ISO DATETIME), null 가능
     * @param to         종료일시(ISO DATETIME), null 가능
     * @return 엑셀 파일 리소스
     */
    @GetMapping("/inventory/logs/excel")
    public Resource downloadLogsExcel(@RequestParam Long materialId,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return inventoryService.downloadExcel(materialId, type, from, to, PageRequest.of(0, Integer.MAX_VALUE)).getBody();
    }
}
