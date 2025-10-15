package com.boot.ict05_final_admin.domain.store.repository;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
}
