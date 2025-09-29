package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NoticeDetailDTO {

    private Long id;

    private NoticeCategory noticeCategory;
    private NoticePriority noticePriority;
    private boolean isShow;
    private String title;
    private String body;
    private String writer;
    private LocalDateTime writerdate;


}
