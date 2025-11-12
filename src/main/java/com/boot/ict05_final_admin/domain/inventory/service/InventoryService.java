package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLogDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;

import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.annotations.Comment;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.n;
import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.s;
import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.d;

/**
 * 기본 재고 서비스 (InventoryService)
 *
 * <p>간단한 오버뷰/합계 등 서비스 파사드.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryLogViewRepository inventoryLogViewRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final StoreNameResolver storeNameResolver;

    /**
     * 재고 목록을 페이지 단위로 조회한다.
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 페이징 처리된 재료 리스트
     */
    @Transactional(readOnly = true)
    public Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable) {
        return inventoryRepository.listInventory(searchDTO, pageable);
    }

    /**
     * 본사 재고 총 건수.
     *
     * @param searchDTO 검색 조건
     * @return 총 건수
     */
    public long countInventory(InventorySearchDTO searchDTO) {
        return inventoryRepository.countInventory(searchDTO);
    }


    /**
     * 본사 입고 등록용 - 재고 선택 목록 조회
     * (재고 + 재료명 출력용)
     */
    @Transactional(readOnly = true)
    public List<Inventory> findAllForSelect() {
        return inventoryRepository.findAll();
    }

    /**
     * 본사 현재고(재료ID 기준)
     *
     * <p>HQ 배치 잔량 합.</p>
     */
    @Comment("HQ 현재고 합계")
    public BigDecimal hqRemainOfMaterial(Long materialId) {
        return inventoryBatchRepository.findHqBatchesForMaterial(materialId).stream()
                .map(b -> b.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 본사 재고 목록을 XLSX로 생성한다.
     *
     * @param inventorySearchDTO 검색 조건
     * @param pageable  클라이언트 전달 페이징(정렬 힌트용). 실제 생성은 전체 덤프
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    public byte[] downloadExcel(InventorySearchDTO inventorySearchDTO, Pageable pageable) throws IOException {
        long total = countInventory(inventorySearchDTO);
        int size = (int) Math.min(Integer.MAX_VALUE, Math.max(1, total));

        PageRequest onePage = PageRequest.of(0, size, pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "updateDate"));

        Page<InventoryListDTO> page = listInventory(inventorySearchDTO, onePage);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("본사재고");
            String[] cols = {"ID","재료ID","재료명","카테고리","현재고","적정수량","판매단위","상태","최종변경일"};

            // header
            Row h = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle();
            Font f = wb.createFont(); f.setBold(true); hs.setFont(f);
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(hs);
            }

            // rows
            int r = 1;
            for (InventoryListDTO v : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getMaterialId());
                row.createCell(2).setCellValue(n(v.getMaterialName()));
                row.createCell(3).setCellValue(n(v.getCategoryName()));
                row.createCell(4).setCellValue(v.getQuantity() == null ? 0d : v.getQuantity().doubleValue());
                row.createCell(5).setCellValue(v.getOptimalQuantity() == null ? 0d : v.getOptimalQuantity().doubleValue());
                row.createCell(6).setCellValue(n(v.getMaterialSalesUnit()));
                row.createCell(7).setCellValue(String.valueOf(v.getStatus()));
                row.createCell(8).setCellValue(v.getUpdateDate() == null ? "" : v.getUpdateDate().toString());
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * 재고 로그를 XLSX로 생성한다.
     *
     * <p>조회된 로그의 storeId 집합을 수집한 뒤, {@link StoreNameResolver}로
     * 가맹점명을 일괄 매핑해 엑셀에 기입한다.</p>
     *
     * @param materialId 재료 ID
     * @param type       로그 유형 필터(옵션)
     * @param startDate  시작일(옵션)
     * @param endDate    종료일(옵션)
     * @param pageable   페이징
     * @return XLSX bytes
     * @throws IOException I/O 오류
     */
    public byte[] downloadLogExcel(Long materialId,
                                   String type,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   Pageable pageable) throws IOException {
        // 1) 로그 페이지 조회
        Page<InventoryLogView> page = inventoryLogViewRepository
                .findLogsByFilter(materialId, type, startDate, endDate, pageable);

        // 2) storeId 집합 → 이름 맵 일괄 조회
        Set<Long> ids = page.getContent().stream()
                .map(InventoryLogView::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nameMap = storeNameResolver.resolveAllWithFallback(ids);

        // 3) 엑셀 생성
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("재고로그_" + materialId);
            String[] cols = { "로그ID","일시","유형","수량","재고후","단가","메모","가맹점명" };

            // 헤더
            Row h = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle();
            Font f = wb.createFont(); f.setBold(true); hs.setFont(f);
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(hs);
            }

            // 데이터
            int r = 1;
            for (InventoryLogView v : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getDate() == null ? "" : v.getDate().toString());
                row.createCell(2).setCellValue(v.getType() == null ? "" : v.getType());
                row.createCell(3).setCellValue(v.getQuantity() == null ? 0d : v.getQuantity().doubleValue());
                row.createCell(4).setCellValue(v.getStockAfter() == null ? 0d : v.getStockAfter().doubleValue());
                row.createCell(5).setCellValue(v.getUnitPrice() == null ? 0d : v.getUnitPrice().doubleValue());
                row.createCell(6).setCellValue(v.getMemo() == null ? "" : v.getMemo());
                String storeName = v.getStoreId() == null ? "" : nameMap.getOrDefault(v.getStoreId(), "");
                row.createCell(7).setCellValue(storeName);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new IllegalStateException("재고 로그 엑셀 생성 실패", e);
        }
    }

    /** 화면용 페이지 조회도 동일 커스텀 메서드를 감싸서 DTO로 내보낼 수 있다. */
    public Page<InventoryLogDTO> getFilteredLogs(Long materialId, String type,
                                                 LocalDate startDate, LocalDate endDate,
                                                 Pageable pageable) {
        Page<InventoryLogView> page =
                inventoryLogViewRepository.findLogsByFilter(materialId, type, startDate, endDate, pageable);

        Set<Long> ids = page.getContent().stream()
                .map(InventoryLogView::getStoreId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long,String> nameMap = storeNameResolver.resolveAllWithFallback(ids);

        List<InventoryLogDTO> list = page.getContent().stream().map(v -> InventoryLogDTO.builder()
                .logId(v.getId())
                .logDate(v.getDate())
                .logType(v.getType())     // 문자열 유지
                .quantity(v.getQuantity())
                .stockAfter(v.getStockAfter())
                .unitPrice(v.getUnitPrice())
                .memo(v.getMemo())
                .storeId(v.getStoreId())
                .storeName(v.getStoreId()==null? null : nameMap.get(v.getStoreId()))
                .build()).toList();

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    /**
     * 재고 배치(로트)를 XLSX로 생성한다.
     *
     * <p>리포지토리 메서드 규약에 맞춰 HQ 배치만 조회한다:
     * {@code InventoryBatchRepository.findHqBatchesForMaterial(materialId)}.</p>
     *
     * @param materialId 재료 ID
     * @param pageable   페이지 파라미터(정렬 힌트 용). 엑셀은 전체 덤프
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     * @throws IllegalStateException 조회/매핑 중 런타임 오류
     */
    public byte[] downloadBatchExcel(Long materialId, Pageable pageable) throws IOException {
        // 1) 데이터 조회
        final List<InventoryBatch> listRaw =
                inventoryBatchRepository.findHqBatchesForMaterial(materialId);
        final List<InventoryBatch> list =
                (listRaw == null) ? java.util.Collections.emptyList() : listRaw;

        log.info("[BATCH-EXCEL] materialId={}, rows={}", materialId, list.size());

        // 2) 엑셀 생성
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final Sheet sheet = wb.createSheet("재고배치_" + materialId);
            final String[] cols = { "배치ID","LOT","입고일","유통기한","입고수량","현재수량","단가" };

            // Header
            final Row h = sheet.createRow(0);
            final CellStyle hs = wb.createCellStyle(); final Font f = wb.createFont(); f.setBold(true); hs.setFont(f);
            for (int i = 0; i < cols.length; i++) { final Cell c = h.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(hs); }

            // Rows
            int r = 1;
            for (InventoryBatch v : list) {
                if (v == null) continue;
                final Row row = sheet.createRow(r++);
                // 숫자/문자 null-safe
                row.createCell(0).setCellValue(s(v.getId()));                      // 문자열로 넣어도 무방
                row.createCell(1).setCellValue(n(v.getLotNo()));
                row.createCell(2).setCellValue(v.getReceivedDate()==null ? "" : v.getReceivedDate().toString());
                row.createCell(3).setCellValue(v.getExpirationDate()==null ? "" : v.getExpirationDate().toString());
                row.createCell(4).setCellValue(d(v.getReceivedQuantity()));
                row.createCell(5).setCellValue(d(v.getQuantity()));
                row.createCell(6).setCellValue(d(v.getUnitPrice()));
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException ioe) {
            log.error("[BATCH-EXCEL] IO error materialId={}", materialId, ioe);
            throw ioe; // I/O는 그대로
        } catch (Exception e) {
            // 어떤 값에서 터지는지 로그로 확인
            log.error("[BATCH-EXCEL] fail materialId={}, cause={}", materialId, e.toString(), e);
            // 엑셀은 빈 시트라도 내려가게 하고 싶으면 주석 해제:
            // return new byte[0];
            throw new IllegalStateException("재고 배치 엑셀 생성 실패: materialId=" + materialId, e);
        }
    }
}
