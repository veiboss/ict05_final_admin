package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialModifyFormDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialWriteFormDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.n;

/**
 * 재료 관련 비즈니스 로직 처리 서비스 클래스
 *
 * <p>재료 등록, 수정, 삭제, 엑셀 다운로드 기능을 제공한다.</p>
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;

    /**
     * 새로운 재료를 등록한다.
     *
     * @param dto 등록할 재료 엔티티
     * @return 저장된 재료 엔티티
     */
    @Transactional
    public Long insertOfficeMaterial(@Valid MaterialWriteFormDTO dto) {

        // 재료 코드 자동 생성 (카테고리 필수)
        String generatedCode = generateMaterialCode(dto.getMaterialCategory());

        Material material = Material.builder()
                .code(generatedCode) // ← 코드 생성
                .materialCategory(dto.getMaterialCategory())
                .name(dto.getName())
                .baseUnit(dto.getBaseUnit())
                .salesUnit(dto.getSalesUnit())
                .conversionRate(dto.getConversionRate())
                .supplier(dto.getSupplier())
                .materialStatus(MaterialStatus.USE)
                .materialTemperature(dto.getMaterialTemperature())
                .optimalQuantity(dto.getOptimalQuantity()) // 적정재고 추가
                .regDate(LocalDateTime.now())
                .build();

        // DB 저장
        Material saved = materialRepository.save(material);

        return saved.getId();
    }

    /**
     * 재료 목록을 페이지 단위로 조회한다.
     *
     * @param materialSearchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 페이징 처리된 재료 리스트
     */
    @Transactional(readOnly = true)
    public Page<MaterialListDTO> selectAllMaterial(MaterialSearchDTO materialSearchDTO, Pageable pageable) {
        return materialRepository.listMaterial(materialSearchDTO, pageable);
    }

    /**
     * ID를 기준으로 재료를 조회한다.
     *
     * @param id 재료 ID
     * @return 재료 엔티티, 존재하지 않으면 null
     */
    @Transactional(readOnly = true)
    public Material findById(Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    /**
     * 기존 재료 정보를 수정한다.
     *
     * @param dto 수정할 데이터
     * @return 수정된 재료 엔티티
     */
    @Transactional
    public Material materialModify(MaterialModifyFormDTO dto) {
        Material material = findById(dto.getId());
        if (material == null) throw new IllegalArgumentException("해당 재료가 존재하지 않습니다.");

        material.updateMaterial(dto);
        materialRepository.save(material);

        // 재고 수량 업데이트 필요
        // inventoryRepository.updateOptimalQuantityByMaterialId(dto.getId(), dto.getOptimalQuantity());   // 본사재고의 적정 수량 반영

        return material;
    }

    /**
     * 재료 상세 정보를 조회한다.
     *
     * @param id 재료 ID
     * @return 재료 엔티티, 존재하지 않으면 null
     */
    public Material detailMaterial(Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    /**
     * 재료 ID를 받아 삭제한다.
     *
     * @param id 재료 ID
     */
    @Transactional
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    /**
     * 재료 목록을 XLSX로 생성한다.
     *
     * <p>헤더는 서비스에서 정의한다. 페이징은 전체 건수를 1페이지로 조회해 일괄 덤프한다.</p>
     *
     * @param materialSearchDTO   재료 검색 조건 DTO
     * @param pageable 스프링 페이징 파라미터(페이지 크기·정렬 힌트). 실제 생성은 전체 덤프
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     * @throws IllegalStateException 리포지토리 접근 등 런타임 오류
     */
    public byte[] downloadExcel(MaterialSearchDTO materialSearchDTO, Pageable pageable) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("재료목록");

            // Header
            String[] cols = { "ID","CODE","카테고리","재료명","기본단위","판매단위","공급업체","상태" };
            Row h = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle(); Font f = wb.createFont(); f.setBold(true); hs.setFont(f);
            for (int i = 0; i < cols.length; i++) { Cell c = h.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(hs); }

            // Data
            long total = materialRepository.countMaterial(materialSearchDTO);
            PageRequest p0 = PageRequest.of(0, (int)Math.min(Integer.MAX_VALUE, total), Sort.by("id").descending());
            Page<MaterialListDTO> page = materialRepository.listMaterial(materialSearchDTO, p0);

            int r = 1;
            for (MaterialListDTO m : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(n(m.getCode()));
                row.createCell(2).setCellValue(String.valueOf(m.getMaterialCategory()));
                row.createCell(3).setCellValue(n(m.getName()));
                row.createCell(4).setCellValue(n(m.getBaseUnit()));
                row.createCell(5).setCellValue(n(m.getSalesUnit()));
                row.createCell(6).setCellValue(n(m.getSupplier()));
                row.createCell(7).setCellValue(String.valueOf(m.getMaterialStatus()));
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(bos);           // IOException 전달
            return bos.toByteArray();
        } catch (IOException ioe) {
            throw ioe;              // 체크예외는 그대로
        } catch (Exception e) {
            throw new IllegalStateException("재료 목록 엑셀 생성 실패", e);
        }
    }

    /** 코드 생성 로직 */
    private String generateMaterialCode(MaterialCategory category) {
        String prefix = category.getCodePrefix(); // Enum에서 직접 가져옴
        String lastCode = materialRepository.findMaxCodeByCategory(category);
        int nextNum = 1;

        if (lastCode != null && lastCode.length() >= 7) {
            try {
                nextNum = Integer.parseInt(lastCode.substring(3)) + 1;
            } catch (NumberFormatException e) {
                log.warn("잘못된 코드 형식: {}", lastCode);
            }
        }

        return String.format("%s%04d", prefix, nextNum);
    }

    /**
     * 카테고리별 재료 목록 조회
     * 본사 입고 등록 시, 선택된 재료 카테고리에 해당하는
     * 사용중(USE) 상태의 재료 목록을 반환한다.
     *
     * @param category 재료 카테고리 (예: BASE, SAUCE 등)
     * @return 조건에 맞는 재료 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MaterialListDTO> findByCategory(MaterialCategory category) {
        return materialRepository.findByCategory(category)
                .stream()
                .map(MaterialListDTO::new)
                .collect(Collectors.toList());
    }

}
