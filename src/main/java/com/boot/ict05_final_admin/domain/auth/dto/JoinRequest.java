package com.boot.ict05_final_admin.domain.auth.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JoinRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
}