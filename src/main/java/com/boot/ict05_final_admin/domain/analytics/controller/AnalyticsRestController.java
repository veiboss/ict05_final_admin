// src/main/java/com/boot/ict05_final_admin/domain/analytics/controller/AnalyticsRestController.java
package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.AnalyticsSearchDto;
import com.boot.ict05_final_admin.domain.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API/analytics")
public class AnalyticsRestController {

	private final AnalyticsService analyticsService;

	/** KPI 리스트 엑셀 다운로드 */
	@GetMapping("/kpi/download")
	public ResponseEntity<Resource> downloadExcelKpiList(
			@ModelAttribute AnalyticsSearchDto cond,
			Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelKpi(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "KPI_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new ByteArrayResource(excelBytes));
	}

	/** 주문 리스트 엑셀 다운로드 */
	@GetMapping("/orders/download")
	public ResponseEntity<Resource> downloadExcelOrdersList(
			@ModelAttribute AnalyticsSearchDto cond,
			Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelOrders(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "Orders_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new ByteArrayResource(excelBytes));
	}

	/** 시간·요일 분석 엑셀 다운로드 */
	@GetMapping("/time/download")
	public ResponseEntity<Resource> downloadExcelTimeList(
			@ModelAttribute AnalyticsSearchDto cond,
			Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelTime(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String mode  = cond.getViewBy() != null ? cond.getViewBy().name() : "DAY";
		String filename = "Time_" + mode + "_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new ByteArrayResource(excelBytes));
	}

	/** KPI 리스트 PDF 다운로드 */
	@GetMapping("/kpi/pdf/download")
	public ResponseEntity<Resource> downloadPdfKpiList(
			@ModelAttribute AnalyticsSearchDto cond
	) {
		byte[] pdfBytes = analyticsService.downloadPdfKpi(cond);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "KPI_" + start + "_" + end + ".pdf";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}

	/** 주문 리스트 PDF 다운로드 */
	@GetMapping("/orders/pdf/download")
	public ResponseEntity<Resource> downloadPdfOrders(
			@ModelAttribute AnalyticsSearchDto cond
	) {
		byte[] pdfBytes = analyticsService.downloadPdfOrders(cond);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String mode  = cond.getViewBy() != null ? cond.getViewBy().name() : "DAY";
		String filename = "Orders_" + mode + "_" + start + "_" + end + ".pdf";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}
}
