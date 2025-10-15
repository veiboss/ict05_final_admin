package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.Data;

@Data
public class MaterialSearchDTO {
    private String s;
    private String type;
    private String size = "10";
}
