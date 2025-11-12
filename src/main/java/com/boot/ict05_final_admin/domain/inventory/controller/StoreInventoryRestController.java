package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.StoreInventoryService;
import com.boot.ict05_final_admin.domain.inventory.service.StoreNameResolver;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelFilename;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 가맹점 재고 REST 컨트롤러
 *
 * <p>이 컨트롤러는 다음과 같은 기능을 제공합니다:</p>
 * <ul>
 *     <li>가맹점의 재고 목록 다운로드</li>
 * </ul>
 *
 * @author 김주연
 * @since 2025.11.12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/store/inventory")
public class StoreInventoryRestController {

    private final StoreInventoryService storeInventoryService;
    private final StoreNameResolver storeNameResolver;

    /**
     * 가맹점 재고 목록 조회
     *
     * @param searchDTO 검색조건 DTO (storeId, 키워드/필드 등)
     * @param page      페이지 번호(0-based), 기본 0
     * @param size      페이지 크기, 기본 10
     * @return Page 형태의 가맹점 재고 목록 DTO
     */
    @GetMapping("/list")
    public Page<StoreInventoryListDTO> listStoreInventory(final StoreInventorySearchDTO searchDTO,
                                                          @RequestParam(defaultValue = "0") final int page,
                                                          @RequestParam(defaultValue = "10") final int size) {
        final Pageable pageable = PageRequest.of(page, size);
        return storeInventoryService.listStoreInventory(searchDTO, pageable);
    }

    /**
     * 가맹점 재고 목록 엑셀 다운로드
     *
     * @param searchDTO 검색 조건
     * @param pageable  페이징
     * @param storeId   가맹점 ID(옵션, null이면 전체)
     */
    @GetMapping("/download")
    @Operation(summary = "가맹점 재고 목록 엑셀 다운로드")
    public ResponseEntity<byte[]> downloadStoreInventory(final StoreInventorySearchDTO searchDTO,
                                                         final Pageable pageable,
                                                         @RequestParam(required = false) final Long storeId)
            throws IOException {
        byte[] xlsx = storeInventoryService.downloadExcel(searchDTO, pageable, storeId);
        String storeName = (storeId == null) ? null : storeNameResolver.resolveOrFallback(storeId);
        return ExcelResponse.ok(xlsx, ExcelFilename.storeInventory(storeName));
    }
}
