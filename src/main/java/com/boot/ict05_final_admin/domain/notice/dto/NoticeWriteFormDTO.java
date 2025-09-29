package com.boot.ict05_final_admin.domain.notice.dto;

import lombok.Data;

@Data
public class NoticeWriteFormDTO {
    private String title;
    private String body;
    private String writer;
}
