package com.boot.ict05_final_admin.domain.receiveOrder.controller;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.service.ReceiveOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "수주현황 API", description = "수주현황 조회/변경 기능 제공")
@Slf4j
public class ReceiveOrderRestController {

    private final ReceiveOrderService receiveOrderService;

    @GetMapping("/receive/detail/{id}")
    public ResponseEntity<ReceiveOrderDetailDTO> detail(@PathVariable Long id, Model model) {
        log.info("GET 요청 수신: id = {}", id);

        ReceiveOrderDetailDTO dto = receiveOrderService.getReceiveOrderDetail(id);

        if (dto == null) {
            // 데이터 없으면 404 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // 데이터 있으면 200 OK 반환
        return ResponseEntity.ok(dto);
    }


}

