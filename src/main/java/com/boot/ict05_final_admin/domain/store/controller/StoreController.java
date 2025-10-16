package com.boot.ict05_final_admin.domain.store.controller;

import com.boot.ict05_final_admin.domain.store.dto.StoreListDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreSearchDTO;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 공지사항 목록을 페이징 처리하여 조회한다.
     *
     * @param storeSearchDTO   (선택) 작성자 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 공지사항 목록 페이지 뷰 이름
     */
    @GetMapping("/store/list")
    public String listOfficeStore(StoreSearchDTO storeSearchDTO,
                                   @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                   Model model,
                                   HttpServletRequest request) {

        // 정렬은 id 기준 내림차순으로 고정(요구 시 동적 정렬로 확장 가능)
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber()-1,     // 1기반 -> 0기반 변환
                pageable.getPageSize(),                   // 페이지 크기 유지
                Sort.by("id").descending()      // id DESC 정렬
        );
        // 서비스 계층 호출: 검색 조건 + 페이징을 적용해 가맹점 목록(Page<StoreListDTO>) 조회
        Page<StoreListDTO> store = storeService.selectAllOfficeStore(storeSearchDTO, pageRequest);

        // 조회 결과(Page 객체)를 모델에 담아 뷰에서 반복/페이지네이션 정보(totalPages 등) 사용 가능
        model.addAttribute("store", store);

        // 현재 요청 URL을 기반으로 한 URI 빌더를 뷰에 전달
        // → 페이징/정렬/검색 파라미터를 유지·변경하며 링크 생성할 때 활용(Thymeleaf에서 편리)
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        // 검색 폼 값 유지(뷰에서 입력값을 그대로 보여주거나 다음 요청에 전달할 때 사용)
        model.addAttribute("storeSearchDTO", storeSearchDTO);

        return "store/list";
    }
}
