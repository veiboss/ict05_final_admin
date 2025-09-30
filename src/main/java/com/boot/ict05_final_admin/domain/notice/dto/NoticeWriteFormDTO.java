package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoticeWriteFormDTO {
    @NotNull(message = "카테고리를 선택해주세요")
    private NoticeCategory noticeCategory;

    @NotNull(message = "상태를 선택해주세요")
    private NoticePriority noticePriority;

    @NotNull(message = "작성자를 입력해주세요")
    private String writer;

    @NotNull(message = "제목을 입력해주세요")
    private String title;

    @NotNull(message = "내용을 입력해주세요")
    private String body;
}
