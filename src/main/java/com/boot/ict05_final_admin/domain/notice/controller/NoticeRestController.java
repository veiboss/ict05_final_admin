package com.boot.ict05_final_admin.domain.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "공지사항 API", description = "공지사항 등록/조회/수정/삭제 기능 제공")
public class NoticeRestController {

    private final NoticeService noticeService;

    @PostMapping("/notice/write")
    @Operation(summary = "공지사항 등록", description = "본사에서 새로운 공지사항을 등록하는 API")
    public ResponseEntity<Long> addOfficeNotice(@RequestBody NoticeWriteFormDTO dto) {
        Long id = noticeService.insertOfficeNotice(dto);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}
