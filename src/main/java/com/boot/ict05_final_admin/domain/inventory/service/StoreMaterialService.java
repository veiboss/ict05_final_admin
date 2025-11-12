package com.boot.ict05_final_admin.domain.inventory.service;


import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.StoreMaterialRepository;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.n;

/**
 * 가맹점 재료 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class StoreMaterialService {

    private final StoreMaterialRepository storeMaterialRepository;

    /**
     * 가맹점 재료 목록을 페이지 단위로 조회한다.
     *
     * @param searchDTO   재료 이름 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 공지사항 리스트 DTO
     */
    @Transactional(readOnly = true)
    public Page<StoreMaterialListDTO> listStoreMaterials(StoreMaterialSearchDTO searchDTO, Pageable pageable) {
        return storeMaterialRepository.listStoreMaterial(searchDTO, pageable);
    }

    /**
     * 검색 결과 총 개수
     */
    @Transactional(readOnly = true)
    public long countStoreMaterials(StoreMaterialSearchDTO searchDTO) {
        return storeMaterialRepository.countStoreMaterial(searchDTO);
    }

    /**
     * 가맹점 재료 엑셀 생성
     *
     * <p>count → 전체 페이지 한 번에 조회 → XLSX 생성.</p>
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable  정렬 힌트용 Pageable
     * @param storeId   가맹점 ID(옵션, null이면 전체)
     * @return XLSX 바이트
     * @throws IOException 워크북 쓰기/닫기 I/O 오류
     * @throws IllegalStateException 생성 실패 시 래핑
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcel(final StoreMaterialSearchDTO searchDTO,
                                final Pageable pageable,
                                final Long storeId) throws IOException {
        long total = countStoreMaterials(searchDTO);
        int size = (int) Math.min(Integer.MAX_VALUE, Math.max(1, total));

        Page<StoreMaterialListDTO> page = listStoreMaterials(
                searchDTO, PageRequest.of(0, size, pageable.getSort())
        );

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("가맹점재료");
            // DTO에 존재가 확실한 필드 중심으로 구성
            String[] cols = { "CODE","재료명","기본단위","판매단위" };

            Row h = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle();
            Font f = wb.createFont(); f.setBold(true); hs.setFont(f);
            for (int i = 0; i < cols.length; i++) { Cell c = h.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(hs); }

            int r = 1;
            for (StoreMaterialListDTO m : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(n(m.getCode()));
                row.createCell(1).setCellValue(n(m.getName()));
                row.createCell(2).setCellValue(n(m.getBaseUnit()));
                row.createCell(3).setCellValue(n(m.getSalesUnit()));
                // 필요 시, DTO에 실제로 존재하면 아래 항목을 추가:
                // row.createCell(4).setCellValue(n(String.valueOf(m.getConversionRate())));
                // row.createCell(5).setCellValue(n(String.valueOf(m.getMaterialStatus())));
                // row.createCell(6).setCellValue(n(m.getStoreName()));
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new IllegalStateException("가맹점 재료 엑셀 생성 실패", e);
        }
    }
}
