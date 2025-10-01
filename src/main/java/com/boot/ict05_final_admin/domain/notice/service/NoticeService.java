package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeSearchDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeAttachment;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeAttachmentRepository;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>공지사항 등록, 수정, 조회, 첨부파일 처리 등의 기능을 제공한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final NoticeAttachmentService noticeAttachmentService;

    /**
     * 새로운 공지사항을 등록하고 첨부파일을 저장한다.
     *
     * @param dto   공지사항 등록 정보 (제목, 내용, 카테고리, 우선순위, 작성자 등)
     * @param files 첨부파일 리스트 (없을 수 있음)
     * @return 저장된 공지사항 ID
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    public Long insertOfficeNotice(NoticeWriteFormDTO dto, List<MultipartFile> files) throws Exception {
        Notice notice = Notice.builder()
                .noticeCategory(dto.getNoticeCategory())
                .noticePriority(dto.getNoticePriority())
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

    /**
     * 작성자 이름으로 필터링하여 공지사항 목록을 페이지 단위로 조회한다.
     *
     * @param noticeSearchDTO   작성자 이름 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 공지사항 리스트 DTO
     */
    public Page<NoticeListDTO> selectAllOfficeNotice(NoticeSearchDTO noticeSearchDTO, Pageable pageable) {
        return noticeRepository.listNotice(noticeSearchDTO, pageable);
    }

    /**
     * ID를 기준으로 공지사항을 조회한다.
     *
     * @param id 공지사항 ID
     * @return 공지사항 엔티티, 존재하지 않으면 null
     */
    public Notice findById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    /**
     * 기존 공지사항을 수정하고 첨부파일을 추가 저장한다.
     *
     * @param dto   수정할 공지사항 정보
     * @param files 첨부파일 리스트 (없을 수 있음)
     * @return 수정된 공지사항 엔티티
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
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

    /**
     * 공지사항 상세 정보를 조회한다.
     *
     * @param id 공지사항 ID
     * @return 공지사항 엔티티, 존재하지 않으면 null
     */
    public Notice detailNotice(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }


    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}
