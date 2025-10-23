package com.boot.ict05_final_admin.domain.menu.controller;


import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuWriteFormDTO;
import com.boot.ict05_final_admin.domain.menu.service.MenuService;
import com.boot.ict05_final_admin.domain.menu.dto.MenuWriteFormDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuModifyFormDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 메뉴 관련 REST API 컨트롤러
 *
 * <p>이 컨트롤러는 다음과 같은 기능을 제공합니다:</p>
 * <ul>
 *     <li>메뉴 등록</li>
 *     <li>메뉴 수정</li>
 * </ul>
 *
 * <p>
 * {@link MenuWriteFormDTO}, {@link MenuModifyFormDTO} 를 통해
 * 검증 및 데이터 바인딩을 수행합니다.</p>
 *
 * @author 채은
 * @since 2025.10.21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name= "메뉴 API", description = "메뉴 등록/조회/수정 기능 제공")
@Slf4j
public class MenuRestController {

    private final MenuService menuService;

    /**
     * 매뉴 등록 API
     *
     * <p>본사에서 새로운 메뉴 등록하는 엔드포인트입니다.
     * 첨부파일을 포함한 메뉴 데이터를 저장합니다.</p>
     *
     * @param dto 등록할 메뉴 데이터 (제목, 내용, 카테고리, 첨부파일 포함)
     * @param bindingResult 유효성 검증 결과
     * @return 등록 성공 여부 및 생성된 메뉴 ID
     * @throws Exception DB 저장 오류
     */
    @PostMapping("/menu/write")
    @Operation(
            summary = "메뉴 등록",
            description = "본사에서 새로운 메뉴를 등록하는 API입니다. 첨부파일도 함께 업로드 가능합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "메뉴 등록 정보",
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
    public ResponseEntity<Map<String, Object>> addStoreMenu(
          @Validated @ModelAttribute MenuWriteFormDTO dto,
          BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError ->  fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return  ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "succes", false,
                            "errors", errors
                    ));
        }

        Long menuId = menuService.insertStoreMenu(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "succes", true,
                        "menuId", menuId
                ));
    }

    /**
     * 메뉴 수정 API
     *
     * <p>기존 메뉴를 수정하는 엔드포인트입니다.
     * 첨부파일 변경/추가도 함께 지원합니다.</p>
     *
     * @param dto 수정할 공지사항 데이터 (제목, 내용, 카테고리, 첨부파일 포함)
     * @param bindingResult 유효성 검증 결과
     * @return 수정 성공 여부 및 수정된 메뉴 ID
     * @throws Exception 파일 처리 오류 또는 DB 저장 오류
     */
    @PostMapping("/menu/modify")
    @Operation(
            summary = "메뉴 수정",
            description = "기존 매뉴를 수정하는 API입니다. 첨부파일 변경/추가도 가능합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "메뉴 수정 정보",
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
    public ResponseEntity<Map<String, Object>> boardModify(
            @Validated @ModelAttribute MenuModifyFormDTO dto,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError ->  fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return  ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "succes", false,
                            "errors", errors
                    ));
        }

        long menuId = menuService.menuModify(dto).getMenuId();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "succes", true,
                        "menuId", menuId
                ));
    }

}
