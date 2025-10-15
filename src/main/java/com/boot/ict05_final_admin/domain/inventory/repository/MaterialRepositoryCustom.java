package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaterialRepositoryCustom {
    Page<MaterialListDTO> listMaterial(MaterialSearchDTO materialSearchDTO, Pageable pageable);
    long countMaterial(MaterialSearchDTO materialSearchDTO);
}
