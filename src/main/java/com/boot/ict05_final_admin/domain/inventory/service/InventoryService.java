package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 본사 재고 서비스
 *
 * <p>재고 목록 조회를 담당한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 목록을 페이지 단위로 조회한다.
     *
     * @param inventorySearchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 페이징 처리된 재료 리스트
     */
    @Transactional(readOnly = true)
    public Page<InventoryListDTO> getInventoryList(InventorySearchDTO inventorySearchDTO, Pageable pageable) {
        return inventoryRepository.listInventory(inventorySearchDTO, pageable);
    }


    /**
     * 본사 입고 등록용 - 재고 선택 목록 조회
     * (재고 + 재료명 출력용)
     */
    public List<HqInventory> findAllForSelect() {
        return inventoryRepository.findAll();
    }

    /**
     * 본사 재고 단건 조회
     *
     * <p>재고 ID를 기준으로 본사 재고 정보를 조회한다.</p>
     *
     * @param id 본사 재고 ID
     * @return HqInventory 엔티티
     */
    @Transactional(readOnly = true)
    public HqInventory findById(Long id) {
        HqInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 본사 재고가 존재하지 않습니다. ID=" + id));

        // === 상태 재계산 (단순화) ===
        if (inventory.getMaterial() != null) {
            inventory.setStatus(
                    InventoryStatus.calculate(inventory.getQuantity(), inventory.getMaterial().getOptimalQuantity())
            );
        }

        return inventory;
    }

    /**
     * 본사 재고 목록을 엑셀 파일로 다운로드한다.
     * @return
     * @throws IOException
     */
    public byte[] downloadExcel(InventorySearchDTO inventorySearchDTO, Pageable pageable)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("재료목록");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("카테고리");
        header.createCell(2).setCellValue("재료명");
        header.createCell(3).setCellValue("현재 수량");
        header.createCell(4).setCellValue("적정 수량");
        header.createCell(5).setCellValue("상태");
        header.createCell(6).setCellValue("마지막 수정일");

        long count = inventoryRepository.countInventory(inventorySearchDTO);
        PageRequest pageRequest = PageRequest.of(0, (int) count, Sort.by("id").descending());
        Page<InventoryListDTO> list = inventoryRepository.listInventory(inventorySearchDTO, pageRequest);

        // ===== 본문 작성 =====
        int i = 1;
        for (InventoryListDTO inv : list) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(inv.getId());
            row.createCell(1).setCellValue(inv.getCategoryName());
            row.createCell(2).setCellValue(inv.getMaterialName());
            row.createCell(3).setCellValue(inv.getQuantity() != null ? inv.getQuantity().toString() : "0");
            row.createCell(4).setCellValue(inv.getOptimalQuantity() != null ? inv.getOptimalQuantity().toString() : "0");
            row.createCell(5).setCellValue(inv.getStatus() != null ? inv.getStatus().getDescription() : "-");
            row.createCell(6).setCellValue(
                    inv.getUpdateDate() != null ? inv.getUpdateDate().toString() : ""
            );
            i++;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
