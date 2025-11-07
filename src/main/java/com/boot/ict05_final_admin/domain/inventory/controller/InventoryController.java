package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.OutLotHistoryRowDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryInService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryLogViewService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryLotService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 조회/삭제 전용 컨트롤러
 *
 * - 페이지 라우팅(Thymeleaf)
 * - 조회용 GET API
 * - 삭제 DELETE API
 */
@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryLotService lotService;
    private final InventoryOutService outService;
    private final InventoryInService inService;
    private final InventoryLogViewService logViewService;

    // -------------------- View routing --------------------

    /**
     * 본사 재고 로그 화면으로 이동한다.
     *
     * @param materialId 재료 ID
     * @param model      뷰 모델
     * @return 템플릿 경로
     */
    @GetMapping("/log/{materialId}")
    public String logPage(@PathVariable Long materialId, Model model) {
        model.addAttribute("materialId", materialId);
        return "admin/inventory/log";
    }

    /**
     * 재료별 배치 현황 화면으로 이동한다.
     *
     * @param materialId 재료 ID
     * @param model      뷰 모델
     * @return 템플릿 경로
     */
    @GetMapping("/batch-status/{materialId}")
    public String batchStatusPage(@PathVariable Long materialId, Model model) {
        model.addAttribute("materialId", materialId);
        return "admin/inventory/batch-status";
    }

    /**
     * 본사 출고 테스트 화면으로 이동한다.
     *
     * @return 템플릿 경로
     */
    @GetMapping("/out_test")
    public String outTestPage() {
        return "admin/inventory/out_test";
    }

    // -------------------- Read APIs (JSON) --------------------

    /**
     * 재료별 배치 현황을 조회한다.
     *
     * @param materialId 재료 ID
     * @return 배치 현황 행 리스트
     */
    @GetMapping("/lot/batch-status")
    @ResponseBody
    public List<BatchStatusRowDTO> batchStatus(@RequestParam Long materialId) {
        return lotService.getBatchStatusForMaterial(materialId)
                .stream()
                .map(r -> BatchStatusRowDTO.builder()
                        .batchId(r.getBatchId())
                        .lotNo(r.getLotNo())
                        .receivedDate(r.getReceivedDate())
                        .expirationDate(r.getExpirationDate())
                        .receivedQty(r.getReceivedQty())
                        .remainQty(r.getRemainQty())
                        .unitPrice(r.getUnitPrice())
                        .build())
                .toList();
    }

    /**
     * 특정 배치의 출고 이력을 페이징 조회한다.
     *
     * @param batchId 배치 ID
     * @param page    페이지 번호(0-base)
     * @param size    페이지 크기
     * @return 출고 이력 페이지
     */
    @GetMapping("/lot/{batchId}/out-history")
    @ResponseBody
    public Page<OutLotHistoryRowDTO> batchOutHistory(@PathVariable Long batchId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return lotService.getOutLotHistory(batchId, PageRequest.of(page, size));
    }

    /**
     * 본사 재고 로그를 필터로 페이징 조회한다.
     *
     * @param materialId 재료 ID
     * @param type       구분(입고/출고/조정) 문자열, null 가능
     * @param startDate  시작일(yyyy-MM-dd), null 가능
     * @param endDate    종료일(yyyy-MM-dd), null 가능
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @return 로그 페이지
     */
    @GetMapping("/logs")
    @ResponseBody
    public Page<InventoryLogView> logs(@RequestParam Long materialId,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) LocalDate startDate,
                                       @RequestParam(required = false) LocalDate endDate,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return logViewService.getFilteredLogs(materialId, type, startDate, endDate, PageRequest.of(page, size));
    }

    // -------------------- Delete APIs --------------------

    /**
     * 출고 헤더를 삭제한다.
     *
     * @param outId 출고 ID
     */
    @DeleteMapping("/out/{outId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOut(@PathVariable Long outId) {
        outService.deleteOut(outId);
    }

    /**
     * 입고 헤더를 삭제한다.
     *
     * @param inId 입고 ID
     */
    @DeleteMapping("/in/{inId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIn(@PathVariable Long inId) {
        inService.deleteIn(inId);
    }

    /**
     * 출고 로트 아이템을 삭제한다.
     *
     * @param lotId 출고-로트 아이템 ID
     */
    @DeleteMapping("/lot/out-item/{lotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOutLotItem(@PathVariable Long lotId) {
        lotService.deleteOutLot(lotId);
    }
}
