package com.boot.ict05_final_admin.domain.store.controller;

import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import com.boot.ict05_final_admin.domain.store.dto.*;
import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import com.boot.ict05_final_admin.domain.store.entity.StoreType;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StaffService staffService;


    /**
     * 공지사항 목록을 페이징 처리하여 조회한다.
     *
     * @param storeSearchDTO (선택) 작성자 이름으로 검색할 경우 전달되는 값
     * @param pageable       페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model          뷰에 전달할 모델 객체
     * @return 공지사항 목록 페이지 뷰 이름
     */
    @GetMapping("/store/list")
    public String listOfficeStore(StoreSearchDTO storeSearchDTO,
                                  @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                  Model model,
                                  HttpServletRequest request) {

        // 정렬은 id 기준 내림차순으로 고정(요구 시 동적 정렬로 확장 가능)
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber() - 1,     // 1기반 -> 0기반 변환
                pageable.getPageSize(),                   // 페이지 크기 유지
                Sort.by("id").descending()      // id DESC 정렬
        );

        // 상태 필터 & 페이지 필터
        boolean isFirstLoad = request.getParameter("status") == null
                && request.getParameter("s") == null
                && request.getParameter("page") == null;
        if (storeSearchDTO.getStatus() != null &&
                storeSearchDTO.getStatus().toString().trim().isEmpty()) {
            storeSearchDTO.setStatus(null);
        }

        Page<StoreListDTO> store = storeService.selectAllOfficeStore(storeSearchDTO, pageRequest);

        model.addAttribute("store", store);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("storeSearchDTO", storeSearchDTO);

        return "store/list";
    }

    /**
     * 가맹점 등록 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 가맹점 등록 작성 페이지 뷰 이름
     */
    @GetMapping("/store/write")
    public String addOfficeStore(Model model) {

        model.addAttribute("storeWriteFormDTO", new StoreWriteFormDTO());
        model.addAttribute("StoreStatus", StoreStatus.values());
        model.addAttribute("StoreType", StoreType.values());
        model.addAttribute("ownerOptions", storeService.ownerOptions());
        model.addAttribute("hqWorkerOptions", storeService.hqWorkerOptions());

        return "store/write";
    }

    /**
     * 특정 가맹점의 상세 내용을 조회한다.
     *
     * @param id    가맹점 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 가맹점 상세 페이지 뷰 이름
     */
    @GetMapping("store/detail/{id}")
    public String detailOfficeStore(@PathVariable Long id, Model model) {
        StoreDetailDTO store = storeService.detailOfficeStore(id);
        model.addAttribute("store", store); // ✅ 이것만
        return "store/detail";
    }


    /**
     * 특정 가맹점의 수정 화면을 표시한다.
     *
     * @param id    가맹점 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 가맹점 수정 페이지 뷰 이름
     */
    @GetMapping("/store/modify/{id}")
    public String modifyOfficeStore(@PathVariable Long id, Model model) {
        // 1) 원본 조회 (Detail DTO 또는 엔티티)

        StoreDetailDTO d = storeService.detailOfficeStore(id);

        // 2) 주소 분리 ( "주소1, 주소2" → userAddress1 / userAddress2 )
        String addr1 = null, addr2 = null;
        if (d.getStoreLocation() != null) {
            String[] parts = d.getStoreLocation().split(",", 2);
            addr1 = parts[0];
            if (parts.length > 1) addr2 = parts[1].trim();
        }

        // 3) 화면용 수정 DTO로 매핑 (필드 타입을 템플릿과 정확히 맞춤)
        StoreModifyFormDTO form = StoreModifyFormDTO.builder()
                .storeId(d.getStoreId())
                .storeName(d.getStoreName())
                .businessRegistrationNumber(d.getBusinessRegistrationNumber())
                .storePhone(d.getStorePhone())
                .storeStatus(d.getStoreStatus())     // Enum
                .storeType(d.getStoreType())         // Enum
                .storeLocation(d.getStoreLocation())
                .userAddress1(addr1)
                .userAddress2(addr2)
                .storeTotalEmployees(toInteger(d.getStoreTotalEmployees()))
                .storeContractTerm(toInteger(d.getStoreContractTerm()))
                .storeContractStartDate(toLocalDate(d.getStoreContractStartDate()))
                .storeContractAffiliateDate(toLocalDate(d.getStoreContractAffiliateDate()))
                .storeAffiliatePrice(d.getStoreAffiliatePrice())
                .storeMonthlySales(d.getStoreMonthlySales())
                .royalty(d.getRoyalty())
                .comment(d.getComment())
                .build();

        // 4) 모델 바인딩 (템플릿은 이 DTO를 기준으로 th:field 렌더)
        model.addAttribute("store", form);
        model.addAttribute("StoreStatus", StoreStatus.values());
        model.addAttribute("StoreType", StoreType.values());
        return "store/modify";
    }

    // ✅ 안전 변환 유틸들: 컨트롤러 클래스 내부에 추가
    private Integer toInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l)    return Math.toIntExact(l); // 범위 체크 포함
        if (v instanceof Number n)  return n.intValue();
        try { return Integer.valueOf(v.toString().trim()); } catch (Exception ignore) { return null; }
    }

    /** 문자열/LocalDate 혼재 대비: 여러 포맷 허용 */
    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        String s = v.toString().trim();
        if (s.endsWith(".")) s = s.substring(0, s.length()-1).trim();

        var fmts = java.util.List.of(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
                java.time.format.DateTimeFormatter.ofPattern("yy. M. d"),
                java.time.format.DateTimeFormatter.ofPattern("yyyy. M. d")
        );
        for (var f : fmts) {
            try { return LocalDate.parse(s, f); } catch (Exception ignore) {}
        }
        return null;
    }


    // ★ 폼 제출을 받는 POST 핸들러
    @PostMapping("/store/modify/{id}")
    public String modifySubmit(@PathVariable Long id,
                               @ModelAttribute("store") StoreModifyFormDTO dto) {
        dto.setStoreId(id);
        Long savedId = storeService.storeModify(dto).getId();
        return "redirect:/store/detail/" + savedId;
    }

}
