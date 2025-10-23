package com.boot.ict05_final_admin.domain.inventory.controller;


import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 본사 재고 REST 컨트롤러.
 *
 * <p>AJAX 요청을 처리한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/inventory")
public class InventoryRestController {

    private final InventoryService inventoryService;

    /**
     * 재고 목록 조회
     */
    @GetMapping("/list")
    public Page<InventoryListDTO> listInventory(
            InventorySearchDTO searchDTO,
            @PageableDefault(size = 20) Pageable pageable) {
        return inventoryService.getInventoryList(searchDTO, pageable);
    }
}
