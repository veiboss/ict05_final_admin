package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
public class NoticeRestController {

    private final NoticeService noticeService;
    @PostMapping("/notice/write")
    public ResponseEntity<Notice> addOfficeNotice(@RequestBody NoticeWriteFormDTO dto) {
        Notice notice = noticeService.insertOfficeNotice(dto);
        return ResponseEntity.ok().body(notice);
    }


}
