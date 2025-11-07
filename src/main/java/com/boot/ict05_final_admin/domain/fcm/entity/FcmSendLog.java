package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_send_log",
        indexes = {
                @Index(name="ix_log_store",  columnList = "store_id_fk"),
                @Index(name="ix_log_member", columnList = "member_id_fk"),
                @Index(name="ix_log_staff",  columnList = "staff_id_fk")
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FcmSendLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmSendLogId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppType appType;

    private String topic;                // null이면 token 발송
    @Column(length = 512) private String token;

    @Column(nullable = false, length = 200)  private String title;
    @Column(nullable = false, length = 1000) private String body;

    @Lob @Column(columnDefinition = "TEXT")   private String dataJson;

    private String resultMessageId;
    private String resultError;

    @Column(nullable = false) private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name="store_id_fk")  private Long storeIdFk;
    @Column(name="member_id_fk") private Long memberIdFk;
    @Column(name="staff_id_fk")  private Long staffIdFk;

    @PrePersist void onCreate() {
        if (sentAt == null) sentAt = LocalDateTime.now();
    }
}
