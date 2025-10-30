package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 본사 재고 목록 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryListDTO {

    /** 재고 ID */
    private Long id;

    /** 재료명 */
    private String materialName;

    /** 카테고리명 */
    private String categoryName;

    /** 현재 수량 */
    private BigDecimal quantity;

    /** 적정 수량 */
    private BigDecimal optimalQuantity;

    /** 재고 상태 */
    @Setter
    private InventoryStatus status;

    /** 마지막 수정일 */
    private LocalDateTime updateDate;
}
