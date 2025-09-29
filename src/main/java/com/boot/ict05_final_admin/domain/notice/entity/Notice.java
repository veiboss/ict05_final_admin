package com.boot.ict05_final_admin.domain.notice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private NoticeCategory noticeCategory;
    private NoticePriority noticePriority;
    private boolean isShow;
    private String title;
    private String body;
    private String writer;

    private LocalDateTime writerdate;
}
