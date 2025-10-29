package com.boot.ict05_final_admin.domain.receiveOrder.service;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.*;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import com.boot.ict05_final_admin.domain.receiveOrder.repository.ReceiveOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 수주(Receive Order) 서비스 클래스
 *
 * <p>본 클래스는 본사에서 관리하는 수주(가맹점 발주) 관련 주요 로직을 처리한다.
 * 다음 기능들을 포함한다:</p>
 *
 * <ul>
 *     <li>수주 목록 조회 (검색 및 페이징)</li>
 *     <li>수주 상세 내역 조회 (하위 품목 포함)</li>
 *     <li>배송 상태 단계별 변경</li>
 *     <li>상단 대시보드 요약 정보 조회</li>
 *     <li>수주 목록 엑셀 다운로드</li>
 * </ul>
 *
 * <p>DB 접근은 {@link ReceiveOrderRepository}를 통해 수행된다.</p>
 *
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ReceiveOrderService {

    private final ReceiveOrderRepository receiveOrderRepository;

    /**
     * 수주 목록을 페이지 단위로 조회한다.
     *
     * @param receiveOrderSearchDTO  (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 수주 리스트 DTO
     */
    public Page<ReceiveOrderListDTO> selectAllOfficeReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        return receiveOrderRepository.listReceive(receiveOrderSearchDTO, pageable);
    }

    /**
     * 특정 수주의 상세 정보를 조회한다.
     *
     * <p>상위 수주({@link ReceiveOrder}) 정보와 함께
     * 하위 품목 목록({@link ReceiveOrderItemDTO})을 함께 반환한다.</p>
     *
     * @param id 수주 ID
     * @return 수주 상세 DTO (품목 리스트 포함)
     * @throws NoSuchElementException 수주가 존재하지 않을 경우
     */
    public ReceiveOrderDetailDTO getReceiveOrderDetail(Long id) {
        ReceiveOrderDetailDTO dto = receiveOrderRepository.findDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("수주 내역이 존재하지 않습니다. id=" + id));

        List<ReceiveOrderItemDTO> items = receiveOrderRepository.findItemsByOrderId(id);
        dto.setItems(items);  // setter로 주입

        return dto;
    }

    /**
     * 수주의 배송 상태를 다음 단계로 변경한다.
     *
     * <p>상태 전환 순서:
     * RECEIVED → PREPARING → SHIPPING → DELIVERED</p>
     *
     * @param id 수주 ID
     * @throws IllegalStateException 이미 배송 완료된 주문일 경우
     * @throws IllegalArgumentException 해당 ID의 수주가 없을 경우
     */
    @Transactional
    public void advanceStatus(Long id) {
        ReceiveOrder order = receiveOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다. id=" + id));

        ReceiveOrderStatus current = order.getStatus();
        ReceiveOrderStatus next;

        switch (current) {
            case RECEIVED -> next = ReceiveOrderStatus.PREPARING;
            case PREPARING -> next = ReceiveOrderStatus.SHIPPING;
            case SHIPPING -> next = ReceiveOrderStatus.DELIVERED;
            default -> throw new IllegalStateException("배송 완료된 주문은 변경할 수 없습니다.");
        }

        order.setStatus(next);
        receiveOrderRepository.save(order);
    }

    /**
     * 수주 현황 요약 데이터를 조회한다.
     *
     * <p>상단 카드에 표시되는 주요 통계 정보를 반환한다.</p>
     *
     * @return 총 주문 수, 총 주문액, 배송 중 수량, 긴급 주문 수 포함 요약 DTO
     */
    public ReceiveOrderSummaryDTO getSummary() {
        return receiveOrderRepository.getSummary();
    }

    /**
     * 수주 목록을 Excel 파일로 생성하여 다운로드한다.
     *
     * <p>검색 조건 및 페이징 정보에 따라 데이터를 조회하고,
     * Apache POI를 이용해 Excel 워크북을 생성한다.</p>
     *
     * @param receiveOrderSearchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 엑셀 파일 데이터 (byte[])
     * @throws IOException Excel 파일 생성 중 오류 발생 시
     */
    public byte[] downloadExcel(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("수주목록");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("가맹점명");
        header.createCell(2).setCellValue("주문번호");
        header.createCell(3).setCellValue("지역");
        header.createCell(4).setCellValue("상태");
        header.createCell(5).setCellValue("우선순위");
        header.createCell(6).setCellValue("주문액");
        header.createCell(7).setCellValue("품목수");
        header.createCell(8).setCellValue("배송예정일");

        long count = receiveOrderRepository.countReceive(receiveOrderSearchDTO);
        PageRequest pageRequest = PageRequest.of(0, (int) count, Sort.by("id").descending());
        Page<ReceiveOrderListDTO> list = receiveOrderRepository.listReceive(receiveOrderSearchDTO, pageRequest);

        int i = 1;
        for (ReceiveOrderListDTO ro : list) {
            Row sheet1_row = sheet.createRow(i);
            sheet1_row.createCell(0).setCellValue(ro.getId());
            sheet1_row.createCell(1).setCellValue(ro.getStoreName());
            sheet1_row.createCell(2).setCellValue(ro.getStoreLocation());
            sheet1_row.createCell(3).setCellValue(String.valueOf(ro.getStatus()));
            sheet1_row.createCell(4).setCellValue(String.valueOf(ro.getPriority()));
            sheet1_row.createCell(5).setCellValue(ro.getTotalPrice() != null ? ro.getTotalPrice().doubleValue() : 0.0);
            sheet1_row.createCell(6).setCellValue(ro.getTotalCount());
            sheet1_row.createCell(7).setCellValue(ro.getDeliveryDate());
            i++;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

}
