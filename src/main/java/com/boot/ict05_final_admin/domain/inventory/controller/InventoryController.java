package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.*;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final MaterialService materialService;
    private final InventoryService inventoryService;
    private final InventoryInService inService;
    private final InventoryOutService outService;
    private final InventoryLotService lotService;
    private final InventoryLogViewService inventoryLogViewService;
    private final InventoryBatchService inventoryBatchService;
    private final InventoryAdjustmentService inventoryAdjustmentService;


    // -------------------- View routing --------------------

    /**
     * 본사 재고 목록을 페이징 처리하여 조회한다.
     *
     * <p>검색 조건과 페이징 정보를 받아 SSR로 목록을 렌더링한다.</p>
     *
     * @param inventorySearchDTO 검색 조건 DTO (재료명, 상태 등)
     * @param pageable           페이징 정보 (페이지 번호, 크기, 정렬 기준)
     * @param model              뷰에 전달할 모델 객체
     * @param request            현재 요청 정보
     * @return 재고 목록 페이지(view)
     */
    @GetMapping("/list")
    public String listInventory(InventorySearchDTO inventorySearchDTO,
                                @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model,
                                HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        Page<InventoryListDTO> inventories = inventoryService.listInventory(inventorySearchDTO, pageRequest);

        model.addAttribute("inventories", inventories);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("inventorySearchDTO", inventorySearchDTO);

        return "inventory/list";
    }

    // 로그 화면에서 fragments/pagination 이 기대하는 urlBuilder 변수를 주입하기 위한 헬퍼
    static final class UrlBuilderHelper {
        public ServletUriComponentsBuilder fromCurrentRequest() {
            return ServletUriComponentsBuilder.fromCurrentRequest();
        }
    }

    /**
     * 본사 재고 로그 화면으로 이동한다.
     *
     * @param materialId 재료 ID
     * @param type       필터: 로그 유형(INCOME/OUTCOME/ADJUST 등) 선택값(옵션)
     * @param startDate  필터: 시작일(옵션)
     * @param endDate    필터: 종료일(옵션)
     * @param page       페이지 인덱스(0-base)
     * @param size       페이지 크기
     * @param model      뷰 모델
     * @return 재고 로그 페이지(view)
     */
    @GetMapping("/log/{materialId}")
    public String logPage(@PathVariable Long materialId,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {

        // 재료
        var material = materialService.findById(materialId);
        String materialName = material != null ? material.getName() : "";

        // 재고: Optional → 실제 엔티티(or null)로 변환
        // findByMaterialId가 Optional<Inventory>를 리턴한다고 가정
        var inventoryOpt = inventoryService.findByMaterialId(materialId); // Optional<Inventory>
        var inventory = inventoryOpt != null ? inventoryOpt.orElse(null) : null;

        // 로그 페이징 (기존 그대로)
        Page<InventoryLogDTO> logs = inventoryLogViewService.getFilteredLogs(
                materialId, type, startDate, endDate, PageRequest.of(page, size));

        // 뷰 모델
        model.addAttribute("logs", logs);
        model.addAttribute("materialId", materialId);
        model.addAttribute("materialName", materialName);

        model.addAttribute("material", material);
        model.addAttribute("inventory", inventory); // ★ Optional 말고 실제 엔티티만 올리기

        model.addAttribute("selectedType", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("urlBuilder", new UrlBuilderHelper());

        return "inventory/log";
    }

    /**
     * 본사 재고 입고 등록 페이지
     *
     * <p>입고 대상 재료를 선택하고, 입고 수량 및 단가를 입력할 수 있는
     * 입고 등록 화면을 렌더링한다.</p>
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 입고 등록 페이지(view)
     */
    @GetMapping("/in/write")
    public String showInventoryInForm(Model model) {
        model.addAttribute("categories", MaterialCategory.values());
        model.addAttribute("inventoryList", inventoryService.findAllForSelect());
        model.addAttribute("now", LocalDateTime.now());
        return "inventory/inventory_in_write";
    }


    /**
     * 본사 출고 테스트 화면으로 이동한다.
     *
     * @return 템플릿 경로
     */
    @GetMapping("/out_test")
    public String outTestPage() {
        return "inventory/out_test";
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
    public Page<InventoryLogDTO> logs(@RequestParam Long materialId,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {

        return inventoryLogViewService.getFilteredLogs(
                materialId,
                type,
                startDate,
                endDate,
                PageRequest.of(page, size)
        );
    }

    // 조정 상세
    @GetMapping("/log/adjust/{logId}")
    @ResponseBody
    public InventoryAdjustDTO getAdjustDetail(@PathVariable Long logId) {
        return inventoryAdjustmentService.getAdjustDetail(logId);
    }

    // LOT 상세
    @GetMapping("/log/lot/{batchId}")
    @ResponseBody
    public InventoryLotDetailDTO getLotDetail(@PathVariable Long batchId) {
        return inventoryBatchService.getLotDetail(batchId);
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
