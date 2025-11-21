package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialModifyFormDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialWriteFormDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.service.MaterialService;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelFilename;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 재료 관련 REST API 컨트롤러.
 *
 * <p>제공 기능:</p>
 * <ul>
 *   <li>재료 등록</li>
 *   <li>재료 수정</li>
 *   <li>재료 목록 엑셀 다운로드</li>
 *   <li>재료 삭제</li>
 *   <li>카테고리별 재료 목록 조회</li>
 * </ul>
 *
 * <p>{@link MaterialWriteFormDTO}, {@link MaterialModifyFormDTO}를 통해
 * 서버 단 유효성 검증 및 데이터 바인딩을 수행한다.</p>
 *
 * <p>도메인 검증/트랜잭션 로직은 서비스 계층에서 처리한다.</p>
 *
 * @author ICT 김주연
 * @since 2025.10.15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/material")
@Tag(name = "재료 API", description = "재료 등록/조회/수정/삭제 기능 제공")
public class MaterialRestController {

    private final MaterialService materialService;

    /**
     * 재료 등록 API.
     *
     * <p>멀티파트/폼 데이터 기반 등록. 바인딩/검증 실패 시 필드별 오류 맵을 반환한다.</p>
     *
     * @param dto            등록할 재료 데이터(이름, 단위, 카테고리, 상태, 첨부 등)
     * @param bindingResult  유효성 검증 결과
     * @return 200 OK: {success:true, id}, 400 BAD REQUEST: {success:false, errors}
     * @throws Exception 파일 업로드 처리 중 오류가 발생한 경우
     */
    @PostMapping("/write")
    @Operation(
            summary = "재료 등록",
            description = "본사에서 새로운 재료를 등록하는 API입니다."
    )
    public ResponseEntity<Map<String, Object>> insertMaterial(
            @Valid @ModelAttribute MaterialWriteFormDTO dto,
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

        Long id = materialService.insertOfficeMaterial(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("success", true, "id", id));
    }

    /**
     * 재료 수정 API.
     *
     * <p>기존 재료를 수정한다. 바인딩/검증 실패 시 필드별 오류 맵을 반환한다.</p>
     *
     * @param dto            수정할 재료 데이터(이름, 단위, 카테고리, 상태, 첨부 등)
     * @param bindingResult  유효성 검증 결과
     * @return 200 OK: {success:true, id}, 400 BAD REQUEST: {success:false, errors}
     * @throws Exception 파일 업로드 처리 중 오류가 발생한 경우
     */
    @PostMapping("/modify")
    @Operation(
            summary = "재료 수정",
            description = "기존 재료 정보를 수정하는 API입니다."
    )
    public ResponseEntity<Map<String, Object>> modifyMaterial(
            @Valid @ModelAttribute MaterialModifyFormDTO dto,
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

        Long id = materialService.materialModify(dto).getId();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }

    /**
     * 재료 목록 엑셀 다운로드 API.
     *
     * <p>검색 조건과 페이징 정보를 바탕으로 화면과 동일한 정렬/필터가 적용된 XLSX를 생성한다.</p>
     *
     * @param searchDTO 검색 조건(옵션)
     * @param pageable  페이징 정보(정렬 힌트로도 사용)
     * @return Excel 파일 바이너리 응답
     * @throws IOException 워크북 생성/쓰기 오류
     */
    @GetMapping("/download")
    @Operation(summary = "재료 목록 엑셀 다운로드", description = "재료 목록을 Excel 파일로 다운로드합니다.")
    public ResponseEntity<byte[]> downloadMaterial(MaterialSearchDTO searchDTO,
                                                   Pageable pageable)
            throws IOException {
        byte[] xlsx = materialService.downloadExcel(searchDTO, pageable);
        return ExcelResponse.ok(xlsx, ExcelFilename.hqMaterial());
    }

    /**
     * 재료 삭제 API.
     *
     * @param id 재료 ID
     * @return 200 OK: {success:true, id}
     */
    @DeleteMapping("/delete")
    @Operation(summary = "재료 삭제", description = "재료 ID를 기준으로 재료 정보를 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteMaterial(@RequestParam("id") Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("success", true, "id", id));
    }

    /**
     * 카테고리별 재료 목록 조회 API.
     *
     * <p>본사 입고 등록 시, 선택된 재료 카테고리에 속하는 본사 사용 재료만 반환한다.</p>
     *
     * @param category 재료 카테고리(예: BASE, SAUCE 등)
     * @return 카테고리 조건에 맞는 재료 목록
     */
    @GetMapping("/list")
    @Operation(summary = "카테고리별 재료 조회", description = "선택된 카테고리에 속한 재료 목록을 반환합니다.")
    public ResponseEntity<List<MaterialListDTO>> getMaterialsByCategory(
            @RequestParam MaterialCategory category) {
        List<MaterialListDTO> list = materialService.findByCategory(category);
        return ResponseEntity.ok(list);
    }
}
