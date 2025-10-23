package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.serivce.StoreMaterialService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Controller
@RequiredArgsConstructor
@RequestMapping("/store-material")
public class StoreMaterialController {

    private final StoreMaterialService storeMaterialService;
    private final StoreService storeService;

    @GetMapping("/list")
    public String listStoreMaterials(StoreMaterialSearchDTO searchDTO,
                                     @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                     Model model,
                                     HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("id").descending());
        Page<StoreMaterialListDTO> storeMaterials = storeMaterialService.listStoreMaterials(searchDTO, pageRequest);

        model.addAttribute("storeMaterials", storeMaterials);
        model.addAttribute("storeMaterialSearchDTO", searchDTO);
        model.addAttribute("stores", storeService.findStoreName());
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "material/list_store";
    }
}
