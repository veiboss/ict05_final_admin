package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeAttachment;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeAttachmentRepository;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@RequiredArgsConstructor
@Service
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final NoticeAttachmentService noticeAttachmentService;

    public Long insertOfficeNotice(NoticeWriteFormDTO dto, List<MultipartFile> files) throws Exception {
        System.out.println("NoticeService - insertOfficeNotice");

        Notice notice = Notice.builder()
                .noticeCategory(NoticeCategory.valueOf(String.valueOf(dto.getNoticeCategory())))
                .noticePriority(NoticePriority.valueOf(String.valueOf(dto.getNoticePriority())))
                .isShow(dto.getIsShow())
                .title(dto.getTitle())
                .body(dto.getBody())
                .writer(dto.getWriter())
                .writerdate(LocalDateTime.now())
                .build();

        // DB 저장
        Notice saved = noticeRepository.save(notice);
        Long id = saved.getId();

        // 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileUrl = noticeAttachmentService.uploadFile(file); // S3, 로컬 등
                    NoticeAttachment attachment = new NoticeAttachment();
                    attachment.setNoticeId(saved.getId());
                    attachment.setUrl(fileUrl);
                    noticeAttachmentRepository.save(attachment);
                }
            }
        }

        return id;
    }


    public Page<NoticeListDTO> selectAllOfficeNotice(String writer, Pageable pageable) {

        return noticeRepository.listNotice(writer, pageable);
    }


    public Notice findById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }


    public Notice noticeModify(NoticeModifyFormDTO dto, List<MultipartFile> files) throws Exception {
        Notice notice = findById(dto.getId());
        if (notice == null) throw new IllegalArgumentException("게시물이 없습니다.");

        notice.updateNotice(dto);

        // 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileUrl = noticeAttachmentService.uploadFile(file); // S3, 로컬 등
                    NoticeAttachment attachment = new NoticeAttachment();
                    attachment.setNoticeId(notice.getId());
                    attachment.setUrl(fileUrl);
                    noticeAttachmentRepository.save(attachment);
                }
            }
        }

        return notice;
    }


    public Notice detailNotice(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }
}
