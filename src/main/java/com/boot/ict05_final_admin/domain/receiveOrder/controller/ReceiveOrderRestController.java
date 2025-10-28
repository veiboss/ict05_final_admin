package com.boot.ict05_final_admin.domain.receiveOrder.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.service.ReceiveOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "수주현황 API", description = "수주 배송 상태 변경 기능 제공")
@Slf4j
public class ReceiveOrderRestController {

    private final ReceiveOrderService receiveOrderService;

    // 배송 상태 업데이트
    @PutMapping("/receive/status/{id}")
    public ResponseEntity<String> updateStatus(@PathVariable Long id) {
        receiveOrderService.advanceStatus(id);
        return ResponseEntity.ok("상태 업데이트 완료");
    }

    /**
     * 수주 엑셀 다운로드 API
     *
     * @param searchDTO 검색 조건
     * @param pageable 페이징 정보
     * @return Excel 파일 바이트 배열
     * @throws IOException 파일 생성 실패 시
     */
    @GetMapping("/receive/download")
    @Operation(summary = "수주 목록 엑셀 다운로드", description = "수주 목록을 Excel 파일로 다운로드합니다.")
    public ResponseEntity<?> downloadMaterial(ReceiveOrderSearchDTO searchDTO, Pageable pageable)
            throws IOException {

        byte[] excelBytes = receiveOrderService.downloadExcel(searchDTO, pageable);

        String filename = "재료목록.xlsx";
        String encodeFilename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + encodeFilename);
        headers.add("Cache-Control", "no-cache");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}

