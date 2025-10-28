package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.service.InventoryInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/API/inventory/in")
@RequiredArgsConstructor
public class InventoryInController {
    private final InventoryInService inventoryInService;

    @PostMapping("/write")
    public ResponseEntity<?> insertInventoryIn(@ModelAttribute InventoryInWriteDTO dto) {
        Long id = inventoryInService.insertInventoryIn(dto);
        return ResponseEntity.ok(Map.of("success", true, "id", id));
    }
}
