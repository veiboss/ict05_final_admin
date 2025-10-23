package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 목록 검색 DTO.
 *
 * <p>검색 조건(카테고리, 재료명, 상태, 가맹점 등)을 전달한다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySearchDTO {

    /** 재료명 검색 */
    private String materialName;

    /** 카테고리명 검색 */
    private String categoryName;

    /** 재고 상태 (SUFFICIENT, LOW, SHORTAGE) */
    private InventoryStatus status;

    /** 가맹점 ID (가맹점 재고 검색 시 사용) */
    private Long storeId;

    /** 검색 유형 구분: HQ(본사) / STORE(가맹점) */
    private String inventoryType;
}
