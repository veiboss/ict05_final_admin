package com.boot.ict05_final_admin.domain.analytics.service;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.repository.AnalyticsRepository;
import com.boot.ict05_final_admin.domain.analytics.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 본사 통계/분석 서비스.
 *
 * <p>QueryDSL 기반 커스텀 리포지토리를 호출해 KPI/주문/재료/시간
 * 관련 집계 데이터를 제공한다. 상단 카드 요약은 YTD(올해 1/1~어제) 고정
 * 구간을 사용하고, 테이블 데이터는 요청 조건 및 페이징에 따른다.</p>
 *
 * <p>조회 계열 메서드는 모두 {@code @Transactional(readOnly = true)}로
 * 설정되어 있어 쓰기 지연/변경 감지 비용을 제거한다.</p>
 *
 * @author
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    /**
     * KPI 상단 요약 카드 조회(YTD).
     *
     * @return KPI 카드 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public KpiCardsDto selectKpiCards() {
        return analyticsRepository.findKpiSummary();
    }

    /**
     * KPI 테이블 목록 조회.
     *
     * @param cond     검색 조건
     * @param pageable 페이징 정보
     * @return KPI 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<KpiRowDto> selectKpis(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findKpi(cond, pageable);
    }

    /**
     * 주문 상단 요약 카드 조회(YTD).
     *
     * @return 주문 카드 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrdersCardsDto selectOrdersCards() {
        return analyticsRepository.findOrdersSummary();
    }

    /**
     * 주문 분석 테이블 목록 조회.
     *
     * @param cond     검색 조건
     * @param pageable 페이징 정보
     * @return 주문 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> selectOrders(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findOrders(cond, pageable);
    }

    /**
     * 재료 상단 요약 카드 조회(YTD).
     *
     * @return 재료 카드 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public MaterialsCardsDto selectMaterialsCards() {
        return analyticsRepository.findMaterialsSummary();
    }

    /**
     * 재료 분석 테이블 목록 조회.
     *
     * @param cond     검색 조건
     * @param pageable 페이징 정보
     * @return 재료 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> selectMaterials(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findMaterials(cond, pageable);
    }

    /** 시간·요일: YTD 누적 차트 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartCardDto selectTimeChartCards() {

        return analyticsRepository.findTimeChartSummary();
    }

    /** 시간·요일: 필터 적용 차트 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartRowDto selectTimeChart(AnalyticsSearchDto cond) {

        return analyticsRepository.findTimeChart(cond);
    }

    /** 시간·요일: 표(상세) */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<TimeRowDto> selectTimeRows(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findTimeRows(cond, pageable);
    }

    /**
     * KPI 행을 엑셀(.xlsx)로 생성하여 바이트 배열로 반환
     *
     * 엑셀 컬럼:
     * Date, Store, Sales, Transaction, UPT, ADS, AUR, Comp.(MoM), Comp.(YoY)
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcelKpi(AnalyticsSearchDto cond, Pageable pageable) {
        // 1) 데이터 조회 (필터 전체를 엑셀로 뽑고 싶으면 Pageable.unpaged() 사용)
        //    페이지 당만 뽑고 싶으면 전달된 pageable 그대로 사용
        Page<KpiRowDto> page = analyticsRepository.findKpi(cond, Pageable.unpaged());
        List<KpiRowDto> rows = page.getContent();

        // 2) 워크북 생성 (스트리밍)
        try (SXSSFWorkbook wb = new SXSSFWorkbook(); // 스트리밍 워크북
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("KPI");
            // sheet.createFreezePane(0,1);

            // --- 스타일 세팅 ---
            DataFormat df = wb.createDataFormat();

            CellStyle head = wb.createCellStyle();
            Font headFont = wb.createFont();
            headFont.setBold(true);
            head.setFont(headFont);
            head.setAlignment(HorizontalAlignment.CENTER);
            head.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            head.setBorderTop(BorderStyle.THIN);
            head.setBorderBottom(BorderStyle.THIN);
            head.setBorderLeft(BorderStyle.THIN);
            head.setBorderRight(BorderStyle.THIN);

            CellStyle text = wb.createCellStyle();
            text.setBorderTop(BorderStyle.THIN);
            text.setBorderBottom(BorderStyle.THIN);
            text.setBorderLeft(BorderStyle.THIN);
            text.setBorderRight(BorderStyle.THIN);

            CellStyle intStyle = wb.createCellStyle();
            intStyle.cloneStyleFrom(text);
            intStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle money = wb.createCellStyle();
            money.cloneStyleFrom(text);
            money.setDataFormat(df.getFormat("#,##0")); // 원화 기호 제외, 테이블과 동일한 숫자 형식

            CellStyle dec1 = wb.createCellStyle();
            dec1.cloneStyleFrom(text);
            dec1.setDataFormat(df.getFormat("0.0"));

            CellStyle pct1 = wb.createCellStyle();
            pct1.cloneStyleFrom(text);
            pct1.setDataFormat(df.getFormat("0.0%")); // NOTE: 0.123 => 12.3%로 표시

            // 3) 헤더
            String[] headers = {
                    "Date","Store","Sales","Transaction","UPT","ADS","AUR","Comp.(MoM)","Comp.(YoY)"
            };
            Row hr = sheet.createRow(0);
            for (int c=0; c<headers.length; c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(head);
            }

            // 4) 본문
            int r = 1;
            for (KpiRowDto dto : rows) {
                Row row = sheet.createRow(r++);

                setText(row,0, dto.getDate(), text);
                setText(row,1, dto.getStoreName(), text);
                setNum (row,2, dto.getSales(), money);             // 매출
                setNum (row,3, dto.getTransaction(), intStyle);     // 건수
                setNum (row,4, dto.getUpt(), dec1);                 // 개/건
                setNum (row,5, dto.getAds(), money);                // 원/건
                setNum (row,6, dto.getAur(), money);                // 원/개
                setPct (row,7, dto.getCompMoM(), pct1);             // % (소수→퍼센트)
                setPct (row,8, dto.getCompYoY(), pct1);             // % (소수→퍼센트)
            }

            // 5) 컬럼 폭 (SXSSF는 autoSize 지원 제한, 수동 설정)
            sheet.setColumnWidth(0, 12 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            for (int c=2; c<9; c++) sheet.setColumnWidth(c, 12 * 256);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("엑셀 생성 실패", e);
        }
    }

    // --- 셀 헬퍼들 ---
    private static void setText(Row row, int col, String val, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        cell.setCellValue(val != null ? val : ""); // 텍스트는 빈 문자열
    }

    private static void setNum(Row row, int col, Number num, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        if (num != null) {
            cell.setCellValue(num.doubleValue());   // null이면 값 안 넣음(빈 셀)
        }
    }

    private static void setPct(Row row, int col, Number pctValue, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        if (pctValue != null) {
            double v = pctValue.doubleValue();
            if (v > 1.0) v = v / 100.0;             // 12.3(%) → 0.123
            cell.setCellValue(v);
        }
    }
}
