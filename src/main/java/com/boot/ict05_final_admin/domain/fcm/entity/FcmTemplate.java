package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fcm_template")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FcmTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmTemplateId;

    @Column(nullable = false, unique = true, length = 64)
    private String templateCode;   // ex) NOTICE_UPDATED, HQ_STOCK_LOW

    @Column(nullable = false, length = 200)
    private String titleTemplate;

    @Column(nullable = false, length = 1000)
    private String bodyTemplate;
}
