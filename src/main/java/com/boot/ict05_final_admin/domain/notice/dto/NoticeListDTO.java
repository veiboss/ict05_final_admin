package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeListDTO {

    private Long id;
    private NoticeCategory noticeCategory;
    private NoticePriority noticePriority;
    private boolean isShow;
    private String title;
    private String body;
    private String writer;
    private LocalDateTime writerdate;

    public String getWriteDate() {
        if (writerdate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return writerdate.format(formatter);
    }
}
