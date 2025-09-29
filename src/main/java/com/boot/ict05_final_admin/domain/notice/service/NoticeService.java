package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeDetailDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepositoryImpl noticeRepository;

    public NoticeWriteFormDTO insertOfficeNotice(NoticeWriteFormDTO noticeDetailDTO) {
        System.out.println("NoticeService - insertOfficeNotice");


        return null;
    }
}
