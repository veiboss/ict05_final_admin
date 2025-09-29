package com.boot.ict05_final_admin.domain.notice.repository;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {
    Page<NoticeListDTO> listNotice(String writer, Pageable pageable);
}
