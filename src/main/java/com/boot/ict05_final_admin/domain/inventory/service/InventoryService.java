package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.*;
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
import java.time.format.DateTimeFormatter;
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


    private final InventoryBatchService inventoryBatchService;
    private final InventoryLotService inventoryLotService;


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
     * 재료 아이디로 재고 조회
     *
     * @param materialId 재료아이디로
     * @return 재고
     */
    public Optional<Inventory> findByMaterialId(Long materialId) {
        return inventoryRepository.findByMaterialId(materialId);
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
     * <p>배치 기준 현재 HQ 재고 합</p>
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
            String[] cols = {"재고ID","재료코드","재료명","카테고리","현재고","판매단위","상태","최종변경일"};

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
                row.createCell(5).setCellValue(n(v.getMaterialSalesUnit()));
                row.createCell(6).setCellValue(String.valueOf(v.getStatus()));
                row.createCell(7).setCellValue(v.getUpdateDate() == null ? "" : v.getUpdateDate().toString());
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
                row.createCell(0).setCellValue(v.getLogId());
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
    @Transactional(readOnly = true)
    public Page<InventoryLogDTO> getFilteredLogs(Long materialId,
                                                 String type,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Pageable pageable) {

        Page<InventoryLogView> page =
                inventoryLogViewRepository.findLogsByFilter(materialId, type, startDate, endDate, pageable);

        // storeId → 가맹점명 매핑
        Set<Long> ids = page.getContent().stream()
                .map(InventoryLogView::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long,String> nameMap = storeNameResolver.resolveAllWithFallback(ids);

        List<InventoryLogDTO> list = page.getContent().stream()
                .map(v -> InventoryLogDTO.builder()
                        .logId(v.getLogId())
                        .logDate(v.getDate())
                        .logType(v.getType())
                        .quantity(v.getQuantity())
                        .stockAfter(v.getStockAfter())
                        .unitPrice(v.getUnitPrice())
                        .memo(v.getMemo())
                        .storeId(v.getStoreId())
                        .storeName(v.getStoreId()==null? null : nameMap.get(v.getStoreId()))
                        .batchId(v.getBatchId())
                        .build())
                .toList();

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    /**
     * 본사 재고 배치(LOT) 목록을 엑셀로 생성한다.
     *
     * <p>해당 재료의 전체 배치(잔량 0 포함)를 화면과 동일한 정렬 기준으로
     * 덤프한다.</p>
     *
     * @param materialId 재료 ID
     * @param pageable   정렬 힌트용 페이징 정보(실제 덤프는 전체)
     * @return XLSX 바이너리
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    @Transactional(readOnly = true)
    public byte[] downloadBatchExcel(Long materialId, Pageable pageable) throws IOException {

        // 화면에서 사용하는 것과 동일한 배치 목록
        List<InventoryBatch> batches =
                inventoryBatchRepository.findAllByMaterial_IdOrderByReceivedDateDesc(materialId);

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Batches");
            int rowIdx = 0;

            // 헤더
            Row header = sheet.createRow(rowIdx++);
            int hc = 0;
            header.createCell(hc++).setCellValue("배치ID");
            header.createCell(hc++).setCellValue("LOT 번호");
            header.createCell(hc++).setCellValue("입고일");
            header.createCell(hc++).setCellValue("유통기한");
            header.createCell(hc++).setCellValue("입고수량");
            header.createCell(hc++).setCellValue("잔량");
            header.createCell(hc++).setCellValue("입고단가");

            // 데이터
            DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (InventoryBatch b : batches) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                row.createCell(c++).setCellValue(b.getId());
                row.createCell(c++).setCellValue(
                        b.getLotNo() != null ? b.getLotNo() : ""
                );
                row.createCell(c++).setCellValue(
                        b.getReceivedDate() != null ? b.getReceivedDate().format(dtfDateTime) : ""
                );
                row.createCell(c++).setCellValue(
                        b.getExpirationDate() != null ? b.getExpirationDate().format(dtfDate) : ""
                );
                row.createCell(c++).setCellValue(
                        b.getReceivedQuantity() != null ? b.getReceivedQuantity().doubleValue() : 0d
                );
                row.createCell(c++).setCellValue(
                        b.getQuantity() != null ? b.getQuantity().doubleValue() : 0d
                );
                row.createCell(c++).setCellValue(
                        b.getUnitPrice() != null ? b.getUnitPrice().doubleValue() : 0d
                );
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * 특정 LOT(배치)의 출고 이력을 엑셀로 생성한다.
     *
     * @param batchId 배치 ID
     * @return XLSX 바이너리
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    /**
     * 특정 LOT(배치)의 출고 이력을 엑셀로 생성한다.
     *
     * @param batchId 배치 ID
     * @return XLSX 바이너리
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    @Transactional(readOnly = true)
    public byte[] downloadLotOutHistoryExcel(Long batchId) throws IOException {

        // LOT 상세(상단 요약 + 파일명용)
        InventoryLotDetailDTO lot = inventoryBatchService.getLotDetail(batchId);

        // 출고 이력 전체 조회 (필요 시 size 조정)
        Page<InventoryOutLotHistoryRowDTO> page =
                inventoryLotService.getOutLotHistory(batchId, PageRequest.of(0, 1000));

        List<InventoryOutLotHistoryRowDTO> rows = page.getContent();

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("LotOutHistory");
            int rowIdx = 0;

            DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            /* ================= LOT 요약 섹션 ================= */

            if (lot != null) {
                // LOT 번호
                Row lotRow0 = sheet.createRow(rowIdx++);
                lotRow0.createCell(0).setCellValue("LOT 번호");
                lotRow0.createCell(1).setCellValue(
                        lot.getLotNo() != null ? lot.getLotNo() : ""
                );

                // 입고일
                Row lotRow1 = sheet.createRow(rowIdx++);
                lotRow1.createCell(0).setCellValue("입고일");
                lotRow1.createCell(1).setCellValue(
                        lot.getReceivedDate() != null
                                ? lot.getReceivedDate().format(dtfDateTime)
                                : ""
                );

                // 입고수량
                Row lotRow2 = sheet.createRow(rowIdx++);
                lotRow2.createCell(0).setCellValue("입고수량");
                lotRow2.createCell(1).setCellValue(
                        lot.getReceivedQuantity() != null
                                ? lot.getReceivedQuantity().doubleValue()
                                : 0d
                );

                // 현재잔량
                Row lotRow3 = sheet.createRow(rowIdx++);
                lotRow3.createCell(0).setCellValue("현재잔량");
                lotRow3.createCell(1).setCellValue(
                        lot.getRemainingQuantity() != null
                                ? lot.getRemainingQuantity().doubleValue()
                                : 0d
                );

                // 유통기한
                Row lotRow4 = sheet.createRow(rowIdx++);
                lotRow4.createCell(0).setCellValue("유통기한");
                lotRow4.createCell(1).setCellValue(
                        lot.getExpirationDate() != null
                                ? lot.getExpirationDate().format(dtfDate)
                                : ""
                );

                // 입고단가
                Row lotRow5 = sheet.createRow(rowIdx++);
                lotRow5.createCell(0).setCellValue("입고단가");
                lotRow5.createCell(1).setCellValue(
                        lot.getUnitPrice() != null
                                ? lot.getUnitPrice().doubleValue()
                                : 0d
                );

                // 요약과 이력 사이 한 줄 비우기
                rowIdx++;
            }

            /* ================= 출고 이력 테이블 ================= */

            // 헤더
            Row header = sheet.createRow(rowIdx++);
            int hc = 0;
            header.createCell(hc++).setCellValue("출고일시");
            header.createCell(hc++).setCellValue("가맹점");
            header.createCell(hc++).setCellValue("출고 수량");
            header.createCell(hc++).setCellValue("메모");

            for (InventoryOutLotHistoryRowDTO r : rows) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                // 출고일시
                String outDateStr = "";
                if (r.getOutDate() != null) {
                    outDateStr = r.getOutDate().format(dtfDateTime);
                }
                row.createCell(c++).setCellValue(outDateStr);

                // 가맹점
                row.createCell(c++).setCellValue(
                        r.getStoreName() != null ? r.getStoreName() : ""
                );

                // 출고 수량 (DTO의 qty 필드만 사용)
                BigDecimal qty = r.getQty();
                row.createCell(c++).setCellValue(
                        qty != null ? qty.doubleValue() : 0d
                );

                // 메모
                row.createCell(c++).setCellValue(
                        r.getMemo() != null ? r.getMemo() : ""
                );
            }

            // 필요하면 자동 너비 조정
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    // Inventory 테이블의 quantity를 배치 합계로 맞춰주는 동기화
    @Transactional
    public void syncInventoryQuantity(Long materialId, BigDecimal newQty) {
        inventoryRepository.findByMaterialId(materialId)
                .ifPresent(inv -> {
                    inv.setQuantity(newQty);   // 엔티티에 setter 또는 change 메서드 있다고 가정
                    inv.updateStatusNow();     // 여니가 말한 상태 재계산 메서드
                });
    }

}
