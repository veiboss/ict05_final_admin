package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.boot.ict05_final_admin.domain.notice.entity.QNotice.notice;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice/write")
    public String addOfficeNotice(Model model) {
        model.addAttribute("NoticeCategory", NoticeCategory.values());
        model.addAttribute("NoticePriority", NoticePriority.values());
        return "notice/write";
    }



    @GetMapping("/notice/list")
    public String listOfficeNotice(@RequestParam(value = "writer", required = false) String writer,
                             @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)
                             Pageable pageable,
                             Model model) {

        PageRequest request = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());

        Page<NoticeListDTO> notices = noticeService.selectAllOfficeNotice(writer, request);
        model.addAttribute("notices", notices);

        System.out.println(notices);

        return "notice/list";
    }

    @GetMapping("/notice/detail/{id}")
    public String detailOfficeNotice(@PathVariable Long id, Model model) {
        Notice notice = noticeService.detailNotice(id);
        model.addAttribute("notice", notice);
        return "notice/detail";
    }

    @GetMapping("/notice/modify/{id}")
    public String modifyOfficeNotice(@PathVariable Long id, Model model) {
        Notice notice = noticeService.detailNotice(id);
        model.addAttribute("notice", notice);
        model.addAttribute("NoticeCategory", NoticeCategory.values());
        model.addAttribute("NoticePriority", NoticePriority.values());

        return "notice/modify";
    }

}
