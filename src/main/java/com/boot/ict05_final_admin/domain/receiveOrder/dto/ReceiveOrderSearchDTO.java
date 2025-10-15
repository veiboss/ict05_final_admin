package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import lombok.Data;

@Data
public class ReceiveOrderSearchDTO {
    private String s;
    private String type;
    private String size = "10";
}
