package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderPriority;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderDetailDTO {

    /** 수주 상세 시퀀스 */
    private Long id;

    /** 수주 코드번호 */
    private String orderCode;

    /** 발주일 */
    private LocalDate orderDate;

    /** 수주 배송예정일 */
    private LocalDate deliveryDate;

    /** 수주 상태 */
    private ReceiveOrderStatus status;

    /** 수주 우선순위 */
    private ReceiveOrderPriority priority;

    /** 가맹점명 */
    private String storeName;   // 가맹점 엔티티

    /** 가맹점 코드 */
    private Long storeId;       // 가맹점 엔티티

    /** 가맹점 지역 */
    private String storeLocation;   // 가맹점 엔티티

    /** 발주 총액 */
    private BigDecimal totalPrice;

    /** 발주 총수량 */
    private Integer totalCount;

    /** 수주 특이사항(비고) */
    private String remark;

    /** 하위 주문 상품 목록 */
    private List<ReceiveOrderItemDTO> items;

    public void setItems(List<ReceiveOrderItemDTO> items) {
        this.items = items;
    }

    /* 화면에 한글 값으로 출력 */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "";
    }

    public String getPriorityDescription() {
        return priority != null ? priority.getDescription() : "";
    }

}
