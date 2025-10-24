package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialWriteFormDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import com.boot.ict05_final_admin.domain.inventory.service.MaterialService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


/**
 * 관리자 재료 관리 컨트롤러
 * <p>
 * 재료 등록, 목록 조회, 상세 조회, 수정 화면을 제공한다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/material")
public class MaterialController {

    private final MaterialService materialService;

    /**
     * 재료 등록 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 공지사항 작성 페이지 뷰 이름
     */
    @GetMapping("/write")
    public String addOfficeMaterial(Model model) {
        model.addAttribute("materialWriteFormDTO", new MaterialWriteFormDTO());
        model.addAttribute("MaterialCategory", MaterialCategory.values());
        model.addAttribute("MaterialTemperature", MaterialTemperature.values());
        return "material/write";
    }

    /**
     * 재료 목록을 페이징 처리하여 조회한다.
     *
     * @param materialSearchDTO   (선택) 작성자 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 재료 목록 페이지 뷰 이름
     */
    @GetMapping("/list")
    public String listMaterial(MaterialSearchDTO materialSearchDTO,
                               @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                               Model model,
                               HttpServletRequest request) {
        System.out.println("MaterialController - listMaterial()");

        boolean isFirstLoad = request.getParameter("status") == null
                && request.getParameter("s") == null
                && request.getParameter("page") == null;
        if (materialSearchDTO.getStatus() != null &&
                materialSearchDTO.getStatus().toString().trim().isEmpty()) {
            materialSearchDTO.setStatus(null);
        }

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
        Page<MaterialListDTO> materials = materialService.selectAllMaterial(materialSearchDTO, pageRequest);

        model.addAttribute("materials", materials);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("materialSearchDTO", materialSearchDTO);

        return "material/list";
    }

    /**
     * 재료의 상세 내용을 조회한다.
     *
     * @param id    재료 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 재료 상세 페이지 뷰 이름
     */
    @GetMapping("/detail/{id}")
    public String detailOfficeMaterial(@PathVariable Long id, Model model) {
        Material material = materialService.detailMaterial(id);
        model.addAttribute("material", material);

        return "material/detail";
    }

    /**
     * 재료의 수정 화면을 표시한다.
     *
     * @param id    재료 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 재료 수정 페이지 뷰 이름
     */
    @GetMapping("/modify/{id}")
    public String modifyOfficeMaterial(@PathVariable Long id, Model model) {
        Material material = materialService.detailMaterial(id);

        model.addAttribute("material", material);
        model.addAttribute("MaterialCategory", MaterialCategory.values());
        model.addAttribute("MaterialTemperature", MaterialTemperature.values());
        model.addAttribute("MaterialStatus", MaterialStatus.values());

        return "material/modify";
    }


    @GetMapping("/delete/{id}")
    public String deleteOfficeMaterial(@PathVariable Long id, Model model) {
        materialService.deleteMaterial(id);
        return "redirect:/material/list";
    }
}
