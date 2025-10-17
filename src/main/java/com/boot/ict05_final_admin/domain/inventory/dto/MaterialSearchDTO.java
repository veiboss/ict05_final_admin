package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import lombok.Data;

@Data
public class MaterialSearchDTO {
    private String s;
    private String type;
    private String size = "10";

    /** 재료 상태 필터 (USE / STOP) */
    private MaterialStatus status;
}
