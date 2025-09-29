package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class NoticeWriteFormDTO {
    private NoticeCategory noticeCategory;
    private NoticePriority noticePriority;
    private String title;
    private String body;
    private String writer;
}
