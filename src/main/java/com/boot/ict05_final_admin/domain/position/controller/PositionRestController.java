package com.boot.ict05_final_admin.domain.position.controller;

import com.boot.ict05_final_admin.domain.position.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API")
@RequiredArgsConstructor
public class PositionRestController {
    private final PositionService positionService;

    @PostMapping("/member/{memberId}/linkStaff")
    @Operation(
            summary = "사원 연동",
            description = "본사에서 회원과 사원을 연동하는 기능입니다",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사원 연동 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "연동 성공",
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
    public ResponseEntity<?> memberLinkStaff(@PathVariable Long memberId) {
        Long id = positionService.memberLinkStaff(memberId).getId();
        return ResponseEntity.ok(Map.of("success", true, "id", id));
    }
}
