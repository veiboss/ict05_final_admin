package com.boot.ict05_final_admin.domain.store.dto;

import lombok.Data;

@Data
public class StoreSearchDTO {
    private String keyword;
    private String type;
    private String size = "10";
}
