package com.boot.ict05_final_admin.domain.member.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.entity.MemberStatus;
import com.boot.ict05_final_admin.domain.member.dto.MemberListDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberSearchDTO;
import com.boot.ict05_final_admin.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/list")
    public String ListOfficeMember(MemberSearchDTO memberSearchDTO,
                                   @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                   Model model,
                                   HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber()-1,
                pageable.getPageSize(),
                Sort.by("id").descending());

        Page<MemberListDTO> members = memberService.selectAllMember(memberSearchDTO, pageRequest);

        model.addAttribute("members", members);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("memberSearchDTO", memberSearchDTO);

        return "member/list";
    }

    @GetMapping("/member/detail/{id}")
    public String detailOfficeMember(@PathVariable Long id, Model model) {
        Member member = memberService.detailMember(id);

        model.addAttribute("member", member);

        return "member/detail";
    }

    @GetMapping("/member/modify/{id}")
    public String modifyOfficeMember(@PathVariable Long id, Model model) {

        Member member = memberService.detailMember(id);

        model.addAttribute("member", member);
        model.addAttribute("MemberStatus", MemberStatus.values());

        return "member/modify";
    }
}
