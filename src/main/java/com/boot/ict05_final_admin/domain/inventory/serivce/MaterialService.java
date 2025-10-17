package com.boot.ict05_final_admin.domain.inventory.serivce;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
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

    /** 재료 목록 조회 */
    public Page<MaterialListDTO> selectAllMaterial(MaterialSearchDTO materialSearchDTO, Pageable pageable){
        return materialRepository.listMaterial(materialSearchDTO, pageable);
    }

    /** 재료 등록 (material_code 자동 생성) */
    public Material saveMaterial(Material material) {
        if (material.getCode() == null || material.getCode().isEmpty()) {
            material.setCode(generateMaterialCode(material.getMaterialCategory()));
        }
        return materialRepository.save(material);
    }

    /** 코드 생성 로직 */
    private String generateMaterialCode(MaterialCategory category) {
        String prefix = category.getCodePrefix(); // Enum에서 직접 가져옴
        String lastCode = materialRepository.findMaxCodeByCategory(category);
        int nextNum = 1;

        if (lastCode != null && lastCode.length() >= 7) {
            try {
                nextNum = Integer.parseInt(lastCode.substring(3)) + 1;
            } catch (NumberFormatException e) {
                log.warn("잘못된 코드 형식: {}", lastCode);
            }
        }

        return String.format("%s%04d", prefix, nextNum);
    }
}
