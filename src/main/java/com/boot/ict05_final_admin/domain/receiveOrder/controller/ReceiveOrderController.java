package com.boot.ict05_final_admin.domain.receiveOrder.controller;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.service.ReceiveOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.data.domain.Pageable;

@Controller
@RequiredArgsConstructor
public class ReceiveOrderController {

    private final ReceiveOrderService receiveOrderService;

    @GetMapping("/receive/list")
    public String listOfficeReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO,
                                    @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable,
                                    Model model,
                                    HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
        Page<ReceiveOrderListDTO> receiveOrder = receiveOrderService.selectAllOfficeReceive(receiveOrderSearchDTO, pageRequest);

        model.addAttribute("receiveOrder", receiveOrder);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("receiveOrderSearchDTO", receiveOrderSearchDTO);

        return "receive/list";
    }
}
