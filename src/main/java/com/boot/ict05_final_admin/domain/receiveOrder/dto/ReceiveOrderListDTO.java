package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderPriority;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderListDTO {

    /** 수주 시퀀스 */
    private Long id;

    /** 가맹점명 */
    private String storeName;

    /** 가맹점 지역 */
    private String storeLocation;

    /** 수주 번호 */
    private String orderCode;

    /** 수주일 */
    private LocalDate orderDate;

    /** 수주 상태 */
    private ReceiveOrderStatus status;

    /** 수주 총액 */
    private BigDecimal totalPrice;

    /** 수주 우선순위 */
    private ReceiveOrderPriority priority;

    /** 수주 비고 */
    private String remark;

    /** 수주 공급업체명 */
    private String supplier;

    /** 수주 배송 예정일 */
    private LocalDate deliveryDate;

    /** 수주 실제 납기일 */
    private LocalDate actualDeliveryDate;

    public String getStatusDescription() {
        return status != null ? status.getDescription() : "";
    }

    public String getPriorityDescription() {
        return priority != null ? priority.getDescription() : "";
    }
}
