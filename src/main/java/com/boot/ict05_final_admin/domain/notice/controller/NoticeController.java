package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice/write")
    public String addOfficeNotice() {
        return "notice/write";
    }


//
//    @GetMapping("/notice/list")
//    public String listOfficeNotice(@RequestParam(value = "writer", required = false) String writer,
//                             @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)
//                             Pageable pageable,
//                             Model model) {
//
//        PageRequest request = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
//
//        //Page<NoticeListDTO> notices = noticeService.selectAllOfficeNotice(writer, request);
//        model.addAttribute("notices", notices);
//        return "notice/list";
//    }
//
//    @GetMapping("/notice/detail/{id}")
//    public String detailOfficeNotice(@PathVariable Long id, Model model) {
//        //Notice notice = noticeService;
//        model.addAttribute("notice", notice);
//        return "notice/detail";
//    }
//
//    @GetMapping("/notice/modify/{id}")
//    public String modifyOfficeNotice(@PathVariable Long id, Model model) {
//        Notice notice = noticeService.;
//        model.addAttribute("notice", notice);
//        return "notice/modify";
//    }

}
