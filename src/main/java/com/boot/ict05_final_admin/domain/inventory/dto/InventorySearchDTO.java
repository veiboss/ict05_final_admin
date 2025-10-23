package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.Data;

/**
 * 재고 목록 검색 DTO.
 *
 * <p>MaterialSearchDTO와 동일한 구조로 단순화.</p>
 */
@Data
public class InventorySearchDTO {

    /** 검색어 (재료명, 카테고리명 등) */
    private String s;

    /** 검색 구분 (HQ: 본사, STORE: 가맹점) */
    private String type;

    /** 페이지 크기 (기본값 10) */
    private String size = "10";

    /** 재고 상태 (SUFFICIENT / LOW / SHORTAGE) */
    private InventoryStatus status;
}
