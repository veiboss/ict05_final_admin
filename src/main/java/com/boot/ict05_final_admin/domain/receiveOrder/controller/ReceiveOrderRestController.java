package com.boot.ict05_final_admin.domain.receiveOrder.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.service.ReceiveOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 수주(Receive Order) 관련 REST API 컨트롤러.
 *
 * <p>본 컨트롤러는 수주 현황 관리와 관련된 비동기 요청을 처리한다.<br>
 * 주요 기능은 다음과 같다:
 * <ul>
 *   <li>수주 상태(배송 진행 단계) 변경</li>
 *   <li>수주 목록 엑셀 파일 다운로드</li>
 * </ul>
 * </p>
 *
 *  @author ICT
 *  @since 2025.10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "수주현황 API", description = "본사 수주 현황 관리용 REST API (상태 변경, 엑셀 다운로드 제공)")
@Slf4j
public class ReceiveOrderRestController {

    private final ReceiveOrderService receiveOrderService;

    /**
     * 수주 배송 상태 업데이트 API
     *
     * <p>지정된 수주의 배송 상태를 다음 단계로 변경한다.<br>
     * 예: 주문 접수 → 배송 중 → 배송 완료</p>
     *
     * @param id 상태를 변경할 수주의 ID
     * @return 상태 업데이트 완료 메시지
     *
     */
    @PutMapping("/receive/status/{id}")
    @Operation(
            summary = "수주 배송 상태 변경",
            description = "특정 수주의 배송 상태를 다음 단계로 전환합니다. 예: 접수 → 배송 중 → 완료.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상태 업데이트 완료",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 수주 ID를 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<String> updateStatus(@PathVariable Long id) {
        receiveOrderService.advanceStatus(id);
        return ResponseEntity.ok("상태 업데이트 완료");
    }

    /**
     * 수주 목록 엑셀 다운로드 API
     *
     * <p>검색 조건과 페이징 정보를 바탕으로 현재 수주 목록을 조회한 뒤,
     * Excel 파일로 생성하여 다운로드할 수 있도록 응답한다.</p>
     *
     * @param searchDTO 검색 필터 조건 (예: 가맹점명, 상태, 기간 등)
     * @param pageable 페이징 정보 (페이지 번호, 사이즈)
     * @return Excel 파일 데이터가 포함된 {@link ResponseEntity} (Content-Disposition 헤더 포함)
     * @throws IOException 파일 생성 실패 시 발생
     *
     */
    @GetMapping("/receive/download")
    @Operation(
            summary = "수주 목록 엑셀 다운로드",
            description = "검색 조건에 따라 수주 목록을 Excel 파일로 다운로드합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "엑셀 파일 생성 성공", content = @Content(mediaType = "application/octet-stream")),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류 (엑셀 생성 실패)")
            }
    )
    public ResponseEntity<?> downloadMaterial(ReceiveOrderSearchDTO searchDTO, Pageable pageable)
            throws IOException {

        byte[] excelBytes = receiveOrderService.downloadExcel(searchDTO, pageable);

        String filename = "수주 목록.xlsx";
        String encodeFilename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + encodeFilename);
        headers.add("Cache-Control", "no-cache");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * 수주 상세 주문서 엑셀 다운로드 API
     *
     * <p>특정 수주 ID를 기반으로 주문 상세 정보(기본 정보 + 주문 상품 내역)를 Excel 파일로 생성하여 다운로드합니다.</p>
     *
     * <p>주문 기본 정보에는 주문번호, 가맹점명, 지역, 상태, 우선순위, 배송예정일이 포함되며,
     * 상품 목록에는 재료명, 카테고리, 수량, 단가, 총액, 재고상태가 표시됩니다.</p>
     *
     * @param id 다운로드할 수주의 고유 ID
     * @return Excel 파일 데이터가 포함된 {@link ResponseEntity}
     * @throws IOException 엑셀 파일 생성 중 오류 발생 시
     *
     * <p><b>Response:</b> 200 OK / application/vnd.openxmlformats-officedocument.spreadsheetml.sheet</p>
     */
    @GetMapping("/receive/download/{id}")
    @Operation(
            summary = "수주 상세 주문서 다운로드",
            description = """
                특정 수주의 상세 주문서를 Excel로 다운로드합니다.
                주문 기본정보(주문번호, 가맹점, 지역, 상태, 우선순위, 배송예정일)와
                주문 상품 리스트(재료명, 수량, 단가, 총액, 재고상태)를 포함합니다.
                """,
            parameters = {
                    @Parameter(name = "id", description = "수주 ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문서 다운로드 성공",
                            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                    @ApiResponse(responseCode = "404", description = "해당 수주를 찾을 수 없음")
            }
    )
    public ResponseEntity<?> downloadReceiveDetail(@PathVariable Long id) throws IOException {

        // Excel 생성
        byte[] excelBytes = receiveOrderService.downloadDetailExcel(id);

        String filename = "수주 상세 주문서_No." + id + ".xlsx";
        String encodeFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + encodeFilename);
        headers.add("Cache-Control", "no-cache");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

}

