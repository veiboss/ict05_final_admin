package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


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


    public Page<NoticeListDTO> selectAllOfficeNotice(String writer, Pageable pageable) {

        return noticeRepository.listNotice(writer, pageable);
    }


    public Notice findById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }


    public Notice noticeModify(NoticeModifyFormDTO dto) {
        System.out.println(dto);
        Notice notice = findById(dto.getId());
        if (notice == null) throw new IllegalArgumentException("게시물이 없습니다.");

        Notice modifiedNotice = notice.toBuilder()
            .id(dto.getId()) // 기존 엔티티 ID 꼭 넣기
            .noticeCategory(NoticeCategory.valueOf(String.valueOf(dto.getNoticeCategory())))
            .noticePriority(NoticePriority.valueOf(String.valueOf(dto.getNoticePriority())))
            .title(dto.getTitle())
            .body(dto.getBody())
            .writerdate(LocalDateTime.now())
            .build();

        return noticeRepository.save(modifiedNotice);
    }


    public Notice detailNotice(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }
}
