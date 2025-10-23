package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import com.boot.ict05_final_admin.domain.inventory.serivce.StoreMaterialService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 가맹점 재료 REST 컨트롤러
 * React 프론트엔드와 통신
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/material")
public class StoreMaterialRestController {

    private final StoreMaterialService storeMaterialService;

    /**
     * 가맹점 재료 목록 조회
     * 예: GET /api/store/material/list?storeId=3&s=우유&type=title&page=0&size=10
     */
    @GetMapping("/list")
    public Page<StoreMaterialListDTO> listStoreMaterials(
            StoreMaterialSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return storeMaterialService.listStoreMaterials(searchDTO, pageable);
    }

    /**
     * 가맹점 재료 단건 조회
     * 예: GET /api/store/material/15
     */
    @GetMapping("/{id}")
    public StoreMaterial getStoreMaterial(@PathVariable Long id) {
        return storeMaterialService.getStoreMaterial(id);
    }

    /**
     * 가맹점 재료 등록 또는 수정
     * 예: POST /api/store/material
     * Body: JSON 형태로 StoreMaterial 데이터
     */
    @PostMapping
    public StoreMaterial saveStoreMaterial(@RequestBody StoreMaterial storeMaterial) {
        return storeMaterialService.save(storeMaterial);
    }

    /**
     * 가맹점 재료 삭제
     * 예: DELETE /api/store/material/15
     */
    @DeleteMapping("/{id}")
    public void deleteStoreMaterial(@PathVariable Long id) {
        storeMaterialService.delete(id);
    }
}
