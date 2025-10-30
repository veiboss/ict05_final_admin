package com.boot.ict05_final_admin.domain.store.controller;

import com.boot.ict05_final_admin.domain.store.dto.StoreModifyFormDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreWriteFormDTO;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "가맹점 API", description = "가맹점 등록/조회/수정 기능 제공")
@Slf4j
public class StoreRestController {

    private final StoreService storeService;

    // =========================
    // 등록
    // =========================
    @PostMapping(
            value = "/store/write",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가맹점 등록",
            description = "본사에서 새로운 가맹점을 등록하는 API입니다."
    )
    public ResponseEntity<Map<String, Object>> addOfficeStaff(
            @Validated @ModelAttribute StoreWriteFormDTO dto,
            BindingResult bindingResult
    ) {
        // DTO 바인딩 확인 로그
        log.info("WRITE DTO = {}", dto);

        if (bindingResult.hasErrors()) {
            log.warn("VALIDATION ERRORS: {}", bindingResult.getFieldErrors());

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : fe.getCode(),
                            (a, b) -> a // 같은 필드 에러가 여러 개면 첫 번째 걸로 유지
                    ));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        try {
            Long id = storeService.insertOfficeStore(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", id
            ));
        } catch (Exception e) {
            log.error("STORE WRITE FAILED", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "서버에서 등록 처리 중 오류가 발생했습니다."
                    ));
        }
    }

    // =========================
    // 수정
    // =========================
    @PostMapping(
            value = "/store/modify",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가맹점 수정",
            description = "기존 가맹점을 수정하는 API입니다."
    )
    public ResponseEntity<Map<String, Object>> modifyStore(
            @Valid @ModelAttribute StoreModifyFormDTO dto,
            BindingResult bindingResult
    ) {
        // DTO 바인딩 확인 로그
        log.info("MODIFY DTO = {}", dto);

        if (bindingResult.hasErrors()) {
            log.warn("VALIDATION ERRORS(MODIFY): {}", bindingResult.getFieldErrors());

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : fe.getCode(),
                            (a, b) -> a
                    ));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        try {
            Long id = storeService.storeModify(dto).getId();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", id
            ));
        } catch (Exception e) {
            log.error("STORE MODIFY FAILED", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "서버에서 수정 처리 중 오류가 발생했습니다."
                    ));
        }
    }
}
