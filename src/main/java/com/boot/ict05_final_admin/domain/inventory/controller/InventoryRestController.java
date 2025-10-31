package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryAdjustDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryInOutService;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 본사 재고 REST API 컨트롤러.
 *
 * <p>본사 재고 조회 및 입고, 출고, 수량 조정 관련 API를 통합 관리한다.</p>
 *
 * <ul>
 *     <li>본사 재고 목록 조회 (GET)</li>
 *     <li>본사 재고 입고 등록 (POST)</li>
 *     <li>본사 재고 출고 등록 (POST)</li>
 *     <li>본사 재고 수량 조정 (POST)</li>
 * </ul>
 *
 * <p>화면 컨트롤러(Thymeleaf)와 분리되어 있으며 JSON 기반으로 동작한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/inventory")
@Tag(name = "본사 재고 API", description = "본사 재고 조회 및 입고 등록 기능 제공")
public class InventoryRestController {

    private final InventoryService inventoryService;
    private final InventoryInOutService inventoryInOutService;

    /**
     * 본사 재고 목록 조회
     *
     * @param searchDTO 검색 조건
     * @param pageable  페이징 설정
     * @return 재고 목록 Page 객체 (JSON)
     */
    @GetMapping("/list")
    @Operation(summary = "본사 재고 목록 조회", description = "검색 조건과 페이징 정보를 기반으로 본사 재고 목록을 조회한다.")
    public ResponseEntity<Page<InventoryListDTO>> listInventory(
            InventorySearchDTO searchDTO,
            Pageable pageable) {
        Page<InventoryListDTO> inventories = inventoryService.getInventoryList(searchDTO, pageable);
        return ResponseEntity.ok(inventories);
    }

    /**
     * 본사 재고 입고 등록
     *
     * @param dto            입고 등록 DTO
     * @param bindingResult  유효성 검증 결과
     * @return 등록 결과 JSON
     */
    @PostMapping("/in/write")
    @Operation(summary = "본사 재고 입고 등록", description = "입고 수량 및 단가 정보를 입력받아 본사 재고를 갱신한다.")
    public ResponseEntity<Map<String, Object>> insertInventoryIn(
            @Validated @ModelAttribute InventoryInWriteDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "errors", errors));
        }

        Long id = inventoryInOutService.insertInventoryIn(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("success", true, "id", id));
    }

    /**
     * 본사 재고 출고 등록
     *
     * @param materialId 출고할 재료 ID
     * @param quantity   출고 수량
     * @param storeId    출고 대상 가맹점 ID (없을 경우 null)
     * @param memo       비고
     * @return 등록 성공 여부 및 출고 ID
     */
    @PostMapping("/out/write")
    @Operation(summary = "본사 재고 출고 등록", description = "가맹점 주문 또는 내부 출고 사유에 따라 본사 재고를 차감하고 출고 이력을 기록한다.")
    public ResponseEntity<Map<String, Object>> insertInventoryOut(
            @RequestParam("materialId") Long materialId,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "memo", required = false) String memo) {

        Long id = inventoryInOutService.insertInventoryOut(materialId, quantity, storeId, memo);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("success", true, "id", id));
    }

    /**
     * 본사 재고 수량 조정
     *
     * <p>입출고 외의 사유(분실, 파손, 오입력 등)로
     * 본사 재고 수량을 직접 수정할 때 사용한다.</p>
     *
     * <p>입력받은 재고 ID(inventoryId)와 수정 수량(quantityAfter)을 기반으로
     * 실제 재고를 갱신하고, 조정 내역(inventory_adjustment)을 로그로 남긴다.</p>
     *
     * @param dto 조정 정보 DTO (재고 ID, 재료 ID, 수정 수량, 사유, 메모)
     * @return 조정 결과 JSON (성공 여부)
     */
    @PostMapping("/adjust")
    @Operation(summary = "본사 재고 수량 조정", description = "입출고 외의 사유(분실, 파손, 오입력 등)로 재고 수량을 직접 수정한다.")
    public ResponseEntity<Map<String, Object>> adjustInventory(@RequestBody InventoryAdjustDTO dto) {
        inventoryInOutService.adjustInventory(dto);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
