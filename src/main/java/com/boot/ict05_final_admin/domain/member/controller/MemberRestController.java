package com.boot.ict05_final_admin.domain.member.controller;

import com.boot.ict05_final_admin.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "회원관리 API", description = "회원 조회/수정/삭제 기능 제공")
@Slf4j
public class MemberRestController {

    private final MemberService memberService;


}
