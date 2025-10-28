package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderSummaryDTO {

    /** 전체 주문 개수 */
    private Long totalCount;

    /** 총 주문액 */
    private BigDecimal totalAmount;

    /** 배송 중인 상품 개수 */
    private Long shippingCount;

    /** 긴급 주문 개수 */
    private Long urgentCount;
}

