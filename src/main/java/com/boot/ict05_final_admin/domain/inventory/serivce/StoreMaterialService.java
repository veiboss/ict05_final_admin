package com.boot.ict05_final_admin.domain.inventory.serivce;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import com.boot.ict05_final_admin.domain.inventory.repository.StoreMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가맹점 재료 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreMaterialService {

    private final StoreMaterialRepository storeMaterialRepository;

    /**
     * 가맹점 재료 목록을 페이지 단위로 조회한다.
     *
     * @param StoreMaterialSearchDTO   재료 이름 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 공지사항 리스트 DTO
     */
    public Page<StoreMaterialListDTO> listStoreMaterials(StoreMaterialSearchDTO searchDTO, Pageable pageable) {
        return storeMaterialRepository.listStoreMaterial(searchDTO, pageable);
    }

    /**
     * 가맹점 재료 단건 조회
     */
    public StoreMaterial getStoreMaterial(Long id) {
        return storeMaterialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료가 존재하지 않습니다. id=" + id));
    }

    /**
     * 가맹점 재료 등록 / 수정
     */
    @Transactional
    public StoreMaterial save(StoreMaterial storeMaterial) {
        return storeMaterialRepository.save(storeMaterial);
    }

    /**
     * 가맹점 재료 삭제
     */
    @Transactional
    public void delete(Long id) {
        if (!storeMaterialRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 재료가 존재하지 않습니다. id=" + id);
        }
        storeMaterialRepository.deleteById(id);
    }

    /**
     * 검색 결과 총 개수
     */
    public long countStoreMaterials(StoreMaterialSearchDTO searchDTO) {
        return storeMaterialRepository.countStoreMaterial(searchDTO);
    }
}
