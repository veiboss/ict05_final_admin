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

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
        Page<StoreListDTO> store = storeService.selectAllOfficeStore(storeSearchDTO, pageRequest);

        model.addAttribute("store", store);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("storeSearchDTO", storeSearchDTO);

        return "store/list";
    }
}
