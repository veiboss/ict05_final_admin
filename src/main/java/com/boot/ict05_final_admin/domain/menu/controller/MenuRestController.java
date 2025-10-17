package com.boot.ict05_final_admin.domain.menu.controller;

import com.boot.ict05_final_admin.domain.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name= "메뉴 API", description = "메뉴 등록/조회/수정/삭제 기능 제공")
@Slf4j
public class MenuRestController {

    private final MenuService menuService;

    @PostMapping("/menu/write")
    @Operation(
            summary = "메뉴 등록",
            description = "본사에서 새로운 공지사항을 등록하는 API입니다. 첨부파일도 함께 업로드 가능합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공지사항 등록 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "등록 성공",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "검증 오류 발생"
                    )
            }
    )

}
