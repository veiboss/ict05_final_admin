package com.boot.ict05_final_admin.domain.staffresources.controller;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffAddFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffListDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffEmploymentType;
import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
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
public class StaffController {

    private final StaffService staffService;
    private final StoreService storeService;

    /**
     * 직원 목록을 페이징 처리하여 조회한다.
     *
     * @param staffSearchDTO   (선택) 작성자 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 직원 목록 페이지 뷰 이름
     */
    @GetMapping("/staff/list")
    public String listStaff(StaffSearchDTO staffSearchDTO,
                            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable,
                            Model model,
                            HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("id").descending());

        Page<StaffListDTO> staffs = staffService.selectAllStaff(staffSearchDTO, pageable);

        model.addAttribute("staffs", staffs);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("staffSearchDTO", staffSearchDTO);

        return "staff/list";
    }

    /**
     * 사원 등록 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 사원등록 작성 페이지 뷰 이름
     */
    @GetMapping("/staff/add")
    public String addForm(Model model) {

        model.addAttribute("staffAddFormDTO", new StaffAddFormDTO());
        model.addAttribute("StaffDepartment", StaffDepartment.values());
        model.addAttribute("StaffEmploymentType", StaffEmploymentType.values());

        return "staff/add";
    }
}
