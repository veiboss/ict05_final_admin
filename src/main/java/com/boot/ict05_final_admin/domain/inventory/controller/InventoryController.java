package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryInOutService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryOverviewService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;

/**
 * 본사 재고 관리 화면 컨트롤러
 *
 * <p>Thymeleaf 기반의 관리자 화면을 렌더링하며,
 * 본사 재고 현황 조회 및 입고 등록 페이지를 제공한다.</p>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryInOutService inventoryInOutService;
    private final InventoryLogViewRepository inventoryLogViewRepository;
    private final InventoryOverviewService inventoryOverviewService;

    /**
     * 본사 재고 목록을 페이징 처리하여 조회한다.
     *
     * <p>검색 조건과 페이징 정보를 기반으로
     * 본사 재고 현황을 조회하고 목록 페이지를 렌더링한다.</p>
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
        Page<InventoryListDTO> inventories = inventoryService.getInventoryList(inventorySearchDTO, pageRequest);

        model.addAttribute("inventories", inventories);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("inventorySearchDTO", inventorySearchDTO);

        return "inventory/list";
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
        model.addAttribute("categories", com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory.values());
        model.addAttribute("inventoryList", inventoryService.findAllForSelect());
        return "inventory/inventory_in_write";
    }

    /**
     * 본사 재고 입고 등록 처리
     *
     * <p>입고 등록 폼에서 제출된 데이터를 기반으로
     * 해당 재료의 재고 수량을 증가시키고, 입고 이력을 저장한다.</p>
     *
     * @param dto 입고 등록 요청 데이터 DTO
     * @return 재고 목록 페이지로 리다이렉트
     */
    @PostMapping("/in/write")
    public String insertInventoryIn(@ModelAttribute InventoryInWriteDTO dto) {
        inventoryInOutService.insertInventoryIn(dto);
        return "redirect:/inventory/list";
    }

    /**
     * 본사 재고 상세 내역 처리
     *
     * <p>선택된 본사 재고 ID를 기준으로 해당 재료의 입출고 내역을
     * 날짜 기준 내림차순으로 페이징 처리하여 상세 페이지를 렌더링한다.</p>
     *
     * @param inventoryId 본사 재고 ID
     * @param pageable    페이징 정보
     * @param model       뷰에 전달할 모델 객체
     * @return 본사 재고 상세(입출고 내역) 페이지(view)
     */
    @GetMapping("/log/{inventoryId}")
    public String viewInventoryLog(
            @PathVariable Long inventoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(page = 1, size = 10, sort = "log_date", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request,
            Model model) {

        HqInventory inventory = inventoryService.findById(inventoryId);
        Long materialId = inventory.getMaterial().getId();

        Pageable corrected = PageRequest.of(
                Math.max(pageable.getPageNumber() - 1, 0),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "log_date")
        );

        // end 포함(+1일)
        LocalDate endPlusOne = (end != null) ? end.plusDays(1) : null;

        Page<InventoryLogView> logs = inventoryLogViewRepository.findLogsByFilter(
                materialId,
                normalizeType(type),
                start,
                endPlusOne,
                corrected
        );

        model.addAttribute("overview", inventoryOverviewService.getOverview(materialId, start, end));

        model.addAttribute("inventory", inventory);
        model.addAttribute("material", inventory.getMaterial());
        model.addAttribute("logs", logs);
        model.addAttribute("selectedType", type);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "inventory/log";
    }


    private String normalizeType(String type) {
        return (type == null || type.isBlank() || "전체".equals(type)) ? null : type;
    }



    /** 출고 테스트 페이지 렌더링 - 삭제예정 */
    @GetMapping("/out_test")
    public String outTestPage() {
        return "inventory/out_test"; // templates/inventory/out_test.html
    }
}
