package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class NoticeController {

     private final NoticeService noticeService;

     //@GetMapping("notice/write")
}
