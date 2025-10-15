package com.boot.ict05_final_admin.domain.store.repository;

import com.boot.ict05_final_admin.domain.store.dto.StoreListDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {
    Page<StoreListDTO> listStore (StoreSearchDTO storeSearchDTO, Pageable pageable);
    long countStore(StoreSearchDTO storeSearchDTO);
}
