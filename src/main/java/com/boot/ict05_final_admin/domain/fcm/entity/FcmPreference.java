package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_preference",
        indexes = {
                @Index(name="ix_pref_member", columnList = "member_id_fk"),
                @Index(name="ix_pref_store",  columnList = "store_id_fk"),
                @Index(name="ix_pref_staff",  columnList = "staff_id_fk")
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FcmPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmPreferenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppType appType;

    @Column(name = "member_id_fk")
    private Long memberIdFk;   // HQ 사용자

    @Column(name = "store_id_fk")
    private Long storeIdFk;    // 가맹점

    @Column(name = "staff_id_fk")
    private Long staffIdFk;    // 가맹점 직원

    @Column(nullable = false) private Boolean catNotice = true;
    @Column(nullable = false) private Boolean catStockLow = true;
    @Column(nullable = false) private Boolean catExpireSoon = true;

    @Column(nullable = false) private Integer thresholdDays = 3;

    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    @PrePersist void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (catNotice == null) catNotice = true;
        if (catStockLow == null) catStockLow = true;
        if (catExpireSoon == null) catExpireSoon = true;
        if (thresholdDays == null) thresholdDays = 3;
    }
}
