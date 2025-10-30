package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialModifyFormDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialWriteFormDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import jakarta.validation.Valid;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 재료 관련 비즈니스 로직 처리 서비스 클래스
 *
 * <p>재료 등록, 수정, 삭제, 엑셀 다운로드 기능을 제공한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * 새로운 재료를 등록한다.
     *
     * @param dto 등록할 재료 엔티티
     * @return 저장된 재료 엔티티
     */
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
    public Material materialModify(MaterialModifyFormDTO dto) {
        Material material = findById(dto.getId());
        if (material == null) throw new IllegalArgumentException("해당 재료가 존재하지 않습니다.");

        material.updateMaterial(dto);

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
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    /**
     * 재료 목록을 엑셀 파일로 다운로드한다.
     * @return
     * @throws IOException
     */
    public byte[] downloadExcel(MaterialSearchDTO materialSearchDTO, Pageable pageable)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("재료목록");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("CODE");
        header.createCell(2).setCellValue("카테고리");
        header.createCell(3).setCellValue("재료명");
        header.createCell(4).setCellValue("기본단위");
        header.createCell(5).setCellValue("판매단위");
        header.createCell(6).setCellValue("공급업체");
        header.createCell(7).setCellValue("상태");

        long count = materialRepository.countMaterial(materialSearchDTO);
        PageRequest pageRequest = PageRequest.of(0, (int) count, Sort.by("id").descending());
        Page<MaterialListDTO> list = materialRepository.listMaterial(materialSearchDTO, pageRequest);

        int i = 1;
        for (MaterialListDTO m : list) {
            Row sheet1_row = sheet.createRow(i);
            sheet1_row.createCell(0).setCellValue(m.getId());
            sheet1_row.createCell(1).setCellValue(m.getCode());
            sheet1_row.createCell(2).setCellValue(String.valueOf(m.getMaterialCategory()));
            sheet1_row.createCell(3).setCellValue(m.getName());
            sheet1_row.createCell(4).setCellValue(m.getBaseUnit());
            sheet1_row.createCell(5).setCellValue(m.getSalesUnit());
            sheet1_row.createCell(6).setCellValue(m.getSupplier());
            sheet1_row.createCell(7).setCellValue(String.valueOf(m.getMaterialStatus()));
            i++;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
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

    /**
     * 재료 정보 수정 및 본사 재고 적정 수량 동기화
     *
     * <p>재료 마스터(Material)의 정보를 수정할 때,
     * 해당 재료의 본사 재고(HqInventory)에 설정된 적정 재고 수량(optimalQuantity)도
     * 동일하게 갱신한다.</p>
     *
     * <p>이 메서드는 재료 마스터와 본사 재고 간의
     * 적정 수량 불일치를 방지하기 위한 수동 동기화 로직이다.</p>
     *
     * @param dto 수정할 재료 정보 DTO
     * @throws IllegalArgumentException 재료가 존재하지 않을 경우 발생
     */
    @Transactional
    public void updateMaterial(MaterialModifyFormDTO dto) {
        Material material = materialRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("재료 없음"));
        material.setOptimalQuantity(dto.getOptimalQuantity());
        materialRepository.save(material);

        // 본사 재고의 적정 수량도 동기화
        inventoryRepository.updateOptimalQuantityByMaterialId(material.getId(), dto.getOptimalQuantity());
    }
}
