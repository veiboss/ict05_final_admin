package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.*;
import com.boot.ict05_final_admin.domain.inventory.service.*;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelFilename;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 본사 재고 관련 REST API 컨트롤러
 *
 * <p>이 컨트롤러는 다음과 같은 기능을 제공합니다:</p>
 * <ul>
 *     <li>출고 등록</li>
 *     <li>입고 등록</li>
 *     <li>재고 조정</li>
 *     <li>매입가 등록</li>
 *     <li>재고 목록 다운로드</li>
 *     <li>재고 로그 목록 다운로드</li>
 *     <li>재고 배치(로트) 목록 다운로드</li>
 * </ul>
 *
 * @author 김주연
 * @since 2025.11.12
 */
@Slf4j
@RestController
@RequestMapping("/API")
@RequiredArgsConstructor
public class InventoryRestController {

    private final InventoryService inventoryService;
    private final InventoryInService inventoryInService;
    private final InventoryOutService inventoryOutService;
    private final InventoryAdjustmentService inventoryAdjustmentService;
    private final UnitPriceService unitPriceService;
    private final MaterialService materialService;


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
        return inventoryOutService.previewFifo(materialId, qty);
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
        return inventoryOutService.confirmOut(
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
    public Long insertInventoryIn(@RequestBody @Valid InventoryInWriteDTO dto) {

        return inventoryInService.insertInventoryIn(dto);
    }


    // -------------------- Adjustment --------------------
    /**
     * 본사 재고 수량 조정을 등록한다.
     *
     * <p>입출고 외의 사유(분실, 파손, 오입력 등)로 재고 수량을 직접 수정한다.</p>
     *
     * @param dto 재고 수량 조정 요청 DTO
     * @return 처리 결과(success 여부)
     */
    @PostMapping("/inventory/adjust")
    public ResponseEntity<Map<String, Object>> adjustInventory(@RequestBody InventoryAdjustDTO dto) {

        log.info("[ADJUST_CTRL] HIT dto={}", dto);  // ★ 컨트롤러 진입 로그

        inventoryAdjustmentService.adjustInventory(dto);
        return ResponseEntity.ok(Map.of("success", true));
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
    @PostMapping("/inventory/unit-price/purchase")
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
    @PutMapping("/inventory/unit-price/purchase/{unitPriceId}")
    public Long updatePurchase(@PathVariable Long unitPriceId,
                               @RequestParam BigDecimal price) {
        return unitPriceService.updatePurchasePrice(unitPriceId, price);
    }

    // -------------------- Download --------------------
    /**
     * 재고 엑셀 다운로드 API
     *
     * @param searchDTO 검색 조건 (재료명, 상태 등)
     * @param pageable 페이징 정보
     * @return Excel 파일 바이트 배열
     */
    @GetMapping("/inventory/download")
    @Operation(summary = "재고 목록 엑셀 다운로드", description = "재고 목록을 Excel 파일로 다운로드합니다.")
    public ResponseEntity<byte[]> downloadInventory(InventorySearchDTO searchDTO, Pageable pageable) throws IOException {
        byte[] xlsx = inventoryService.downloadExcel(searchDTO, pageable);
        return ExcelResponse.ok(xlsx, ExcelFilename.hqInventory());
    }

    /**
     * 재고 로그 엑셀 다운로드 API
     *
     * <p>화면 필터(유형/기간/페이징)를 그대로 적용해 재료별 로그를 XLSX로 생성한다.</p>
     *
     * @param materialId 재료 ID
     * @param type       로그 유형(INCOME/OUTCOME/ADJUST 등), 옵션
     * @param startDate  시작일(포함), 옵션
     * @param endDate    종료일(포함), 옵션
     * @param page       페이지 인덱스(기본 0). 서비스 내부에서는 전체 덤프로 생성 가능
     * @param size       페이지 크기(기본 10). 서비스 내부에서는 전체 덤프로 생성 가능
     * @return XLSX 바이너리 응답
     * @throws java.io.IOException 워크북 생성·쓰기 오류
     */
    @Operation(summary = "본사 재고 로그 엑셀 다운로드", description = "재료별 재고 로그를 Excel 파일로 다운로드합니다.")
    @GetMapping("/inventory/{materialId}/log/download")
    public ResponseEntity<byte[]> downloadInventoryLog(@PathVariable Long materialId,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size)
            throws IOException {
        byte[] xlsx = inventoryService.downloadLogExcel(
                materialId, type, startDate, endDate, PageRequest.of(page, size)
        );

        String materialName = Optional.ofNullable(materialService.findById(materialId))
                .map(m -> m.getName())
                .orElse(null);

        return ExcelResponse.ok(xlsx, ExcelFilename.inventoryLogByName(materialName));
    }


    /**
     * 본사 재고 배치(로트) 엑셀 다운로드 API
     *
     * <p>HQ 배치(가맹점 미지정, 잔량 &gt; 0)를 유통기한↑ → 입고일↑ 순으로 전체 덤프한다.</p>
     *
     * @param materialId 재료 ID
     * @param page       페이지 인덱스(기본 0). 엑셀은 전체 덤프이나 정렬 힌트로 수집
     * @param size       페이지 크기(기본 10). 엑셀은 전체 덤프이나 정렬 힌트로 수집
     * @return XLSX 바이너리 응답
     * @throws java.io.IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    @Operation(summary = "본사 재고 배치 엑셀 다운로드")
    @GetMapping("/inventory/{materialId}/batch/download")
    public ResponseEntity<byte[]> downloadInventoryBatch(@PathVariable Long materialId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size)
            throws IOException {
        byte[] xlsx = inventoryService.downloadBatchExcel(materialId, PageRequest.of(page, size));

        // 재료명 조회 후 파일명 생성. 없으면 “재고배치_YYYY...”로 처리
        String materialName = Optional.ofNullable(materialService.findById(materialId))
                .map(m -> m.getName())
                .orElse(null);
        String filename = ExcelFilename.inventoryBatchByName(materialName);

        return ExcelResponse.ok(xlsx, filename);
    }
}
