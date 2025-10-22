package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuAllergyRepository extends JpaRepository<Allergy, Long> {

    /**
     * 특정 메뉴에 연결된 알레르기 목록 조회
     * @param menuId 메뉴 시퀀스
     * @return MenuAllergy 리스트
     */
    List<Allergy> findByMenuId(Long menuId);

}
