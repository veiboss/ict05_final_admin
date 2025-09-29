package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeDetailDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Long insertOfficeNotice(NoticeWriteFormDTO dto) {
        System.out.println("NoticeService - insertOfficeNotice");

        Notice notice = Notice.builder()
                .noticeCategory(NoticeCategory.valueOf(String.valueOf(dto.getNoticeCategory())))
                .noticePriority(NoticePriority.valueOf(String.valueOf(dto.getNoticePriority())))
                .isShow(true)
                .title(dto.getTitle())
                .body(dto.getBody())
                .writer(dto.getWriter())
                .writerdate(LocalDateTime.now())
                .build();

        // DB 저장
        Notice saved = noticeRepository.save(notice);
        Long id = saved.getId();

        return id;
    }
}
