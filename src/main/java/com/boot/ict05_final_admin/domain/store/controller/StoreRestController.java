package com.boot.ict05_final_admin.domain.store.controller;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffModifyFormDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreModifyFormDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreWriteFormDTO;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "가맹점 API", description = "가맹점 등록/조회/수정 기능 제공")
@Slf4j
public class StoreRestController {

    private final StoreService storeService;

    @PostMapping("/store/write")
    @Operation(
            summary = "가맹점 등록",
            description = "본사에서 새로운 가맹점을 등록하는 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "가맹점 등록 정보",
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
    public ResponseEntity<Map<String, Object>> addOfficeStaff(
            @Validated @ModelAttribute StoreWriteFormDTO dto,
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

        Long id = storeService.insertOfficeStore(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }

    /**
     * 가맹점 수정 API
     *
     * <p>기존 가맹점을 수정하는 엔드포인트입니다.</p>
     *
     * @param dto 수정할 가맹점 데이터 (이름, 단위, 카테고리, 상태, 첨부파일 등)
     * @param bindingResult 유효성 검증 결과
     * @return 수정 성공 여부 및 재료 ID
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    @PostMapping("/store/modify")
    @Operation(
            summary = "가맹점 수정",
            description = "기존 가맹점 수정하는 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "가맹점 수정 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
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
    public ResponseEntity<Map<String, Object>> modifyStore(
            @Valid @ModelAttribute StoreModifyFormDTO dto,
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

        Long id = storeService.storeModify(dto).getId();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }
}
