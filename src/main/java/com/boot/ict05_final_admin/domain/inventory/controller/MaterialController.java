package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.serivce.MaterialService;
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
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping("/material/list")
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
}
