package com.boot.ict05_final_admin.domain.staffresources.controller;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffAddFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "인사/급여 API", description = "사원 등록/조회/수정/삭제 기능 제공")
@Slf4j
public class StaffRestController {

    private final StaffService staffService;

    @PostMapping("/staff/add")
    @Operation(
            summary = "신규 사원 등록",
            description = "본사에서 새로운 사원을 등록하는 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사원 등록 정보",
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
    public ResponseEntity<Map<String, Object>> addOfficeNotice(
            @Validated @ModelAttribute StaffAddFormDTO dto,
            BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        Long id = staffService.insertOfficeStaff(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }
}
