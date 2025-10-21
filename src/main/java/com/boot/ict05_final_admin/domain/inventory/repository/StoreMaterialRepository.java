package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreMaterialRepository extends JpaRepository<StoreMaterial, Long>, StoreMaterialRepositoryCustom {
}
