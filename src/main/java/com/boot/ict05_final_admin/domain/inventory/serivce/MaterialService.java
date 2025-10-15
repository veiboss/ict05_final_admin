package com.boot.ict05_final_admin.domain.inventory.serivce;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class MaterialService {
    private final MaterialRepository materialRepository;

    public Page<MaterialListDTO> selectAllMaterial(MaterialSearchDTO materialSearchDTO, Pageable pageable){
        return materialRepository.listMaterial(materialSearchDTO, pageable);
    }
}
