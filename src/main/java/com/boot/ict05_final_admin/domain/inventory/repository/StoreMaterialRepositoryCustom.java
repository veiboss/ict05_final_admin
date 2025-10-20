package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreMaterialRepositoryCustom {
    Page<StoreMaterialListDTO> listStoreMaterial(StoreMaterialSearchDTO searchDTO, Pageable pageable);
    long countStoreMaterial(StoreMaterialSearchDTO searchDTO);
}
