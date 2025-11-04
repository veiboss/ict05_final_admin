package com.boot.ict05_final_admin.domain.analytics.service;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

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