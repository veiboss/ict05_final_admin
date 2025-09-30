package com.boot.ict05_final_admin.domain.notice.entity;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
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

    @Enumerated(EnumType.STRING)
    private NoticeCategory noticeCategory;
    @Enumerated(EnumType.STRING)
    private NoticePriority noticePriority;
    private Boolean isShow;
    private String title;
    private String body;
    private String writer;
    private LocalDateTime writerdate;

    // 엔티티 내부에서 상태 변경
    public void updateNotice(NoticeModifyFormDTO dto) {
        this.noticeCategory = NoticeCategory.valueOf(String.valueOf(dto.getNoticeCategory()));
        this.noticePriority = NoticePriority.valueOf(String.valueOf(dto.getNoticePriority()));
        this.isShow = true;
        this.title = dto.getTitle();
        this.body = dto.getBody();
        this.writer = dto.getWriter();
        this.writerdate = LocalDateTime.now();
    }
}
