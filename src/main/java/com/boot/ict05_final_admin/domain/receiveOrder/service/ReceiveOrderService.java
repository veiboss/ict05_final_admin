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

    /** 주문 상세 정보 조회 */
    public ReceiveOrderDetailDTO getReceiveOrderDetail(Long id) {
        ReceiveOrderDetailDTO dto = receiveOrderRepository.findDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("수주 내역이 존재하지 않습니다. id=" + id));

        List<ReceiveOrderItemDTO> items = receiveOrderRepository.findItemsByOrderId(id);
        dto.setItems(items);  // setter로 주입

        return dto;
    }

    /** 배송 상태 변경 */
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

    /** 상단 카드 데이터 */
    public ReceiveOrderSummaryDTO getSummary() {
        return receiveOrderRepository.getSummary();
    }

    /**
     * 수주 목록을 엑셀 파일로 다운로드한다.
     * @return
     * @throws IOException
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
