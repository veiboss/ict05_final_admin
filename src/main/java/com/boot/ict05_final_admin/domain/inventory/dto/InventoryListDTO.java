package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 목록 DTO.
 *
 * <p>본사/가맹점 재고 목록 공통 구조로 사용된다.</p>
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

    /** 카테고리명 (선택) */
    private String categoryName;

    /** 현재 재고 수량 */
    private BigDecimal quantity;

    /** 적정 재고 수량 */
    private BigDecimal optimalQuantity;

    /** 재고 상태 */
    private InventoryStatus status;

    /** 상태 설명 */
    private String statusDescription;

    /** 마지막 업데이트 일시 */
    private LocalDateTime updateDate;

    /** 가맹점명 (본사일 경우 null) */
    private String storeName;

    /**
     * Enum의 한글 설명 반환.
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : null;
    }
}
