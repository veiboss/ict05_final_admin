package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.StoreInventoryService;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 본사가 가맹점 재고 현황을 조회하는 컨트롤러
 *
 * <p>조회 전용 (수정/삭제 없음)</p>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/inventory/store")
public class StoreInventoryController {

    private final StoreInventoryService storeInventoryService;
    private final StoreService storeService;

    /**
     * 가맹점 재고 목록 조회
     *
     * @param searchDTO 검색 조건 (상태, 검색어 등)
     * @param pageable 페이징 객체
     * @param model 모델 객체
     * @param request 요청 정보
     * @return inventory/list_store.html
     */
    @GetMapping({"/list"})
    public String listStoreInventories(StoreInventorySearchDTO searchDTO,
                                       @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)
                                       Pageable pageable,
                                       Model model,
                                       HttpServletRequest request) {

        // 페이징 설정 (1페이지 → 0 index)
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("id").descending()
        );

        // 조회
        Page<StoreInventoryListDTO> inventories =
                storeInventoryService.listStoreInventories(searchDTO, pageRequest);

        // 모델 바인딩
        model.addAttribute("inventories", inventories);
        model.addAttribute("storeInventorySearchDTO", searchDTO);
        model.addAttribute("stores", storeService.findStoreName());
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "inventory/list_store";
    }
}
