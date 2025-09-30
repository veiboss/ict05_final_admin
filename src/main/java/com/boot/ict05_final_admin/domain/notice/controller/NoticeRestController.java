package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "공지사항 API", description = "공지사항 등록/조회/수정/삭제 기능 제공")
@Slf4j
public class NoticeRestController {

    private final NoticeService noticeService;

    @PostMapping("/notice/write")
    @Operation(summary = "공지사항 등록", description = "본사에서 새로운 공지사항을 등록하는 API")
    public ResponseEntity<Map<String, Object>> addOfficeNotice(@Validated @ModelAttribute NoticeWriteFormDTO dto,
                                                               BindingResult bindingResult) throws Exception {

//        NoticeWriteFormDTO dto = new NoticeWriteFormDTO();
//        dto.setNoticeCategory(noticeCategory);
//        dto.setNoticePriority(noticePriority);
//        dto.setWriter(writer);
//        dto.setTitle(title);
//        dto.setBody(body);

        // 검증 오류 체크
        if (bindingResult.hasErrors()) {
            // FieldError 정보를 Map으로 변환해서 전달
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // 400
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        // 검증 통과 시 DB 저장
        Long id = noticeService.insertOfficeNotice(dto, dto.getFiles());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }

    @PostMapping("/notice/modify")
    public ResponseEntity<Long> boardModify(@RequestBody NoticeModifyFormDTO dto) {

        Notice notice = noticeService.noticeModify(dto);

        return ResponseEntity.status(HttpStatus.OK).body(notice.getId());
    }


}
