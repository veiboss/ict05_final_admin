package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import lombok.Data;

@Data
public class NoticeModifyFormDTO {
    private Long id;
    private NoticeCategory noticeCategory;
    private NoticePriority noticePriority;
    private String title;
    private String body;
    private String writer;
}
