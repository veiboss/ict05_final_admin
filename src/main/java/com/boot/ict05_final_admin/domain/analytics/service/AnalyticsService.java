package com.boot.ict05_final_admin.domain.analytics.service;

import com.boot.ict05_final_admin.config.PythonPdfClient;
import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.repository.AnalyticsRepository;
import com.boot.ict05_final_admin.domain.analytics.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final PythonPdfClient pythonPdfClient;

    @LogExecutionTime
    @Transactional(readOnly = true)
    public KpiCardsDto selectKpiCards() {
        return analyticsRepository.findKpiSummary();
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<KpiRowDto> selectKpis(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findKpi(cond, pageable);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrdersCardsDto selectOrdersCards() {
        return analyticsRepository.findOrdersSummary();
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> selectOrders(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findOrders(cond, pageable);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public MaterialsCardsDto selectMaterialsCards() {
        return analyticsRepository.findMaterialsSummary();
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> selectMaterials(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findMaterials(cond, pageable);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartCardDto selectTimeChartCards() {
        return analyticsRepository.findTimeChartSummary();
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartRowDto selectTimeChart(AnalyticsSearchDto cond) {
        return analyticsRepository.findTimeChart(cond);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<TimeRowDto> selectTimeRows(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findTimeRows(cond, pageable);
    }

    @Transactional(readOnly = true)
    public byte[] downloadExcelKpi(AnalyticsSearchDto cond, Pageable pageable) {
        long total = analyticsRepository.countKpi(cond);
        if (total == 0) {
            return new byte[0];
        }

        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<KpiRowDto> page = analyticsRepository.findKpi(cond, fullPage);
        List<KpiRowDto> rows = page.getContent();

        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("KPI");
            DataFormat df = wb.createDataFormat();

            CellStyle head = createHeaderStyle(wb);
            CellStyle text = createBodyStyle(wb);
            CellStyle money = createMoneyStyle(wb, text, df);
            CellStyle intStyle = createIntegerStyle(wb, text, df);
            CellStyle dec1 = createDecimalStyle(wb, text, df, "0.0");
            CellStyle pct1 = createPercentStyle(wb, text, df, "0.0%");

            String[] headers = {"Date","Store","Sales","Transaction","UPT","ADS","AUR","Comp.(MoM)","Comp.(YoY)"};
            Row hr = sheet.createRow(0);
            for (int c=0; c<headers.length; c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(head);
            }

            int r = 1;
            for (KpiRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                setText(row,0, dto.getDate(), text);
                setText(row,1, dto.getStoreName(), text);
                setNum (row,2, dto.getSales(), money);
                setNum (row,3, dto.getTransaction(), intStyle);
                setNum (row,4, dto.getUpt(), dec1);
                setNum (row,5, dto.getAds(), money);
                setNum (row,6, dto.getAur(), money);
                setPct (row,7, dto.getCompMoM(), pct1);
                setPct (row,8, dto.getCompYoY(), pct1);
            }

            sheet.setColumnWidth(0, 12 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            for (int c=2; c<9; c++) sheet.setColumnWidth(c, 12 * 256);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] downloadExcelOrders(AnalyticsSearchDto cond, Pageable pageable) {
        long total = analyticsRepository.countOrders(cond);
        if (total == 0) {
            return new byte[0];
        }

        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<OrdersRowDto> page = analyticsRepository.findOrders(cond, fullPage);
        List<OrdersRowDto> rows = page.getContent();

        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Orders");
            DataFormat df = wb.createDataFormat();

            CellStyle head = createHeaderStyle(wb);
            CellStyle text = createBodyStyle(wb);
            CellStyle money = createMoneyStyle(wb, text, df);
            CellStyle intStyle = createIntegerStyle(wb, text, df);

            boolean isDailyView = cond.getViewBy() == ViewBy.DAY;

            List<String> headerList = new ArrayList<>(Arrays.asList("Date", "Store", "MenuCount", "MenuSales", "OrderCount", "OrderSales"));
            if (isDailyView) {
                headerList.add(1, "OrderDate");
                headerList.add(3, "OrderId");
                headerList.add(4, "Category");
                headerList.add(5, "Menu");
                headerList.add("OrderType");
            }

            Row hr = sheet.createRow(0);
            for (int c = 0; c < headerList.size(); c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headerList.get(c));
                cell.setCellStyle(head);
            }

            int r = 1;
            for (OrdersRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                int col = 0;
                setText(row, col++, dto.getDate(), text);
                if (isDailyView) {
                    setText(row, col++, dto.getOrderDate(), text);
                }
                setText(row, col++, dto.getStoreName(), text);
                if (isDailyView) {
                    setText(row, col++, dto.getOrderId() != null ? dto.getOrderId().toString() : "", text);
                    setText(row, col++, dto.getCategory(), text);
                    setText(row, col++, dto.getMenu(), text);
                }
                setNum(row, col++, dto.getMenuCount(), intStyle);
                setNum(row, col++, dto.getMenuSales(), money);
                setNum(row, col++, dto.getOrderCount(), intStyle);
                setNum(row, col++, dto.getOrderSales(), money);
                if (isDailyView) {
                    setText(row, col++, dto.getOrderType(), text);
                }
            }

            for (int c = 0; c < headerList.size(); c++) {
                sheet.setColumnWidth(c, 15 * 256);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] downloadExcelTime(AnalyticsSearchDto cond, Pageable pageable) {
        // 1) 전체 건수만 먼저 확보 (limit 1)
        long total = analyticsRepository.countTime(cond);
        if (total == 0) return new byte[0];

        // 2) 전체 로우 한번에 조회
        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<TimeRowDto> page = analyticsRepository.findTimeRows(cond, fullPage);
        List<TimeRowDto> rows = page.getContent();

        // 3) 엑셀 작성
        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Time");
            DataFormat df = wb.createDataFormat();

            CellStyle head    = createHeaderStyle(wb);
            CellStyle text    = createBodyStyle(wb);
            CellStyle money   = createMoneyStyle(wb, text, df);
            CellStyle intStyle= createIntegerStyle(wb, text, df);

            boolean isDailyView = (cond.getViewBy() == ViewBy.DAY);

            // UI 컬럼 구성과 동일하게 맞춤
            List<String> headerList = new ArrayList<>();
            if (isDailyView) {
                headerList = Arrays.asList(
                        "Store", "시간대", "요일", "주문ID", "주문금액",
                        "카테고리", "메뉴", "OrderType", "OrderDate"
                );
            } else {
                headerList = Arrays.asList(
                        "Date", "Store", "시간대", "요일", "주문금액", "OrderType"
                );
            }

            // 헤더
            Row hr = sheet.createRow(0);
            for (int c = 0; c < headerList.size(); c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headerList.get(c));
                cell.setCellStyle(head);
            }

            // 본문
            int r = 1;
            for (TimeRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                int col = 0;

                if (isDailyView) {
                    setText(row, col++, dto.getStoreName(),  text);
                    setText(row, col++, dto.getHourSlot(),   text);
                    setText(row, col++, dto.getDayOfWeek(),  text);
                    // 주문ID는 가끔 null/Total일 수 있으므로 문자열로
                    setText(row, col++, dto.getOrderId() != null ? dto.getOrderId().toString() : "", text);
                    setNum (row, col++, dto.getOrderAmount(), money);
                    setText(row, col++, dto.getCategory(),   text);
                    setText(row, col++, dto.getMenu(),       text);
                    setText(row, col++, dto.getOrderType(),  text);
                    setText(row, col++, dto.getOrderDate(),  text);
                } else {
                    setText(row, col++, dto.getDate(),       text);
                    setText(row, col++, dto.getStoreName(),  text);
                    setText(row, col++, dto.getHourSlot(),   text);
                    setText(row, col++, dto.getDayOfWeek(),  text);
                    setNum (row, col++, dto.getOrderAmount(), money);
                    setText(row, col++, dto.getOrderType(),  text);
                }
            }

            // 컬럼 폭
            for (int c = 0; c < headerList.size(); c++) {
                int w = switch (headerList.get(c)) {
                    case "Store"     -> 18;
                    case "시간대"       -> 14;
                    case "요일"        -> 8;
                    case "주문ID"       -> 12;
                    case "주문금액"      -> 14;
                    case "카테고리"      -> 16;
                    case "메뉴"        -> 20;
                    case "OrderType" -> 12;
                    case "OrderDate" -> 20;
                    case "Date"      -> 12;
                    default -> 15;
                };
                sheet.setColumnWidth(c, w * 256);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBodyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook wb, CellStyle base, DataFormat df) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat("#,##0"));
        return style;
    }

    private CellStyle createIntegerStyle(Workbook wb, CellStyle base, DataFormat df) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat("#,##0"));
        return style;
    }

    private CellStyle createDecimalStyle(Workbook wb, CellStyle base, DataFormat df, String format) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat(format));
        return style;
    }

    private CellStyle createPercentStyle(Workbook wb, CellStyle base, DataFormat df, String format) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat(format));
        return style;
    }

    /**
     * KPI 행을 PDF(.pdf)로 생성하여 바이트 배열로 반환
     */
    @Transactional(readOnly = true)
    public byte[] downloadPdfKpi(AnalyticsSearchDto cond) {
        // ✅ 엑셀과 동일한 패턴로 '풀페이지' Pageable 사용
        long total = analyticsRepository.countKpi(cond);
        if (total == 0) return new byte[0];

        Pageable fullPage = PageRequest.of(0, (int) total); // ← 핵심
        List<KpiRowDto> rows = analyticsRepository.findKpi(cond, fullPage).getContent();

        rows = new ArrayList<>(rows); // 직렬화 안전

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("title", "KPI 분석 리포트");
        criteria.put("startDate", cond.getStartDate().format(DateTimeFormatter.ISO_DATE));
        criteria.put("endDate", cond.getEndDate().format(DateTimeFormatter.ISO_DATE));

        Map<String, Object> payload = new HashMap<>();
        payload.put("criteria", criteria);
        payload.put("data", rows);

        return pythonPdfClient.generateKpiReportPdf(payload);
    }

    /**
     * 주문 행을 PDF(.pdf)로 생성하여 바이트 배열로 반환
     */
    private static final int MAX_PDF_ROWS = 5_000;

    @Transactional(readOnly = true)
    public byte[] downloadPdfOrders(AnalyticsSearchDto cond) {
        long total = analyticsRepository.countOrders(cond);

        boolean truncated = false;
        int fetchSize = (int) Math.min(total, MAX_PDF_ROWS);
        if (total > MAX_PDF_ROWS) truncated = true;

        Pageable fullPage = PageRequest.of(0, Math.max(fetchSize, 1));
        List<OrdersRowDto> rawRows = analyticsRepository.findOrders(cond, fullPage).getContent();

        if (rawRows.size() > fetchSize && fetchSize > 0) {
            rawRows = rawRows.subList(0, fetchSize);
        }

        // DTO -> Map (직렬화 100% 안전)
        List<Map<String, Object>> rows = new ArrayList<>(rawRows.size());
        for (OrdersRowDto d : rawRows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date",       nz(d.getDate()));
            m.put("orderDate",  nz(d.getOrderDate()));
            m.put("storeName",  nz(d.getStoreName()));
            m.put("category",   nz(d.getCategory()));
            m.put("menu",       nz(d.getMenu()));
            m.put("menuCount",  d.getMenuCount());
            m.put("menuSales",  d.getMenuSales());
            m.put("orderCount", d.getOrderCount());
            m.put("orderSales", d.getOrderSales());
            m.put("orderType",  nz(d.getOrderType()));
            rows.add(m);
        }
        if (rows.isEmpty()) {
            rows.add(new LinkedHashMap<>()); // ReportLab 테이블 최소 1행 보정
        }

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("title", "주문 분석 리포트");
        criteria.put("viewBy",   cond.getViewBy() != null ? cond.getViewBy().name() : "DAY");
        criteria.put("startDate", cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "");
        criteria.put("endDate",   cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "");
        criteria.put("rowCount",  rows.size());
        criteria.put("totalCount", total);
        criteria.put("truncated", truncated);

        Map<String, Object> payload = new HashMap<>();
        payload.put("criteria", criteria);
        payload.put("data", rows);

        byte[] pdf = pythonPdfClient.generateOrdersReportPdf(payload);
        log.info("Orders PDF ready: total={}, sentRows={}, bytes={}", total, rows.size(), (pdf == null ? 0 : pdf.length));
        return pdf;
    }

    private static String nz(String s) { return (s == null) ? "" : s; }

    // --- 셀 헬퍼들 ---
    private static void setText(Row row, int col, String val, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        cell.setCellValue(val != null ? val : "");
    }

    private static void setNum(Row row, int col, Number num, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        if (num != null) {
            cell.setCellValue(num.doubleValue());
        }
    }

    private static void setPct(Row row, int col, Number pctValue, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        if (pctValue != null) {
            double v = pctValue.doubleValue();
            if (v > 1.0) v = v / 100.0;
            cell.setCellValue(v);
        }
    }
}