package com.boot.ict05_final_admin.domain.store.service;

import com.boot.ict05_final_admin.domain.store.dto.StoreListDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreSearchDTO;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가맹점 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>가맹점 등록, 수정, 상세 조회 처리 등의 기능을 제공한다.</p>
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    /**
     * 가맹점 이름으로 필터링하여 공지사항 목록을 페이지 단위로 조회한다.
     *
     * @param storeSearchDTO   작성자 이름 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 공지사항 리스트 DTO
     */
    public Page<StoreListDTO> selectAllOfficeStore(StoreSearchDTO storeSearchDTO, Pageable pageable) {
        return storeRepository.listStore(storeSearchDTO, pageable);
    }

    /**
     * ID를 기준으로 공지사항을 조회한다.
     *
     * @param id 공지사항 ID
     * @return 공지사항 엔티티, 존재하지 않으면 null
     */
    public Store findById(Long id) {
        return storeRepository.findById(id).orElse(null);
    }
}
