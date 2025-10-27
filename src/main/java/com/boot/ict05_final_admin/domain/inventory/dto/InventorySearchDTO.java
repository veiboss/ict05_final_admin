package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.Data;

/**
 * 본사 재고 검색 DTO
 */
@Data
public class InventorySearchDTO {
    private String s;
    private String type;
    private String size = "10";

    /** 재고 상태 (SUFFICIENT / LOW / SHORTAGE) */
    private InventoryStatus status;
}
