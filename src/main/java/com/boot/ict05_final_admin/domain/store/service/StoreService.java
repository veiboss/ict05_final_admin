package com.boot.ict05_final_admin.domain.store.service;

import com.boot.ict05_final_admin.domain.store.dto.FindStoreDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreWriteFormDTO;
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

import java.util.List;

/**
 * 가맹점 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>특징</p>
 * <ul>
 *   <li>컨트롤러와 리포지토리 사이에서 트랜잭션과 도메인 규칙을 담당</li>
 *   <li>목록/단건 조회 등 읽기 기능 제공(추후 등록/수정/삭제 확장)</li>
 * </ul>
 *
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class StoreService {

    private final StoreRepository storeRepository; // 데이터 접근(기본 CRUD + 커스텀 쿼리) 의존성

    /**
     * 가맹점 이름으로 필터링하여 공지사항 목록을 페이지 단위로 조회한다.
     *
     * @param storeSearchDTO   작성자 이름 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 공지사항 리스트 DTO
     */
    public Page<StoreListDTO> selectAllOfficeStore(StoreSearchDTO storeSearchDTO, Pageable pageable) {
        return storeRepository.listStore(storeSearchDTO, pageable);   // Querydsl 커스텀 리포지토리 호출
    }

    /**
     * 새로운 가맹점을 등록한다.
     *
     * @param dto 가맹점 등록 정보
     * @return 저장된 가맹점 ID
     */
    public long insertOfficeStore(StoreWriteFormDTO dto) {

        String address = "";
        String address1 = dto.getUserAddress1();
        String address2 = dto.getUserAddress2();
        address = address1 + "," + address2;
        dto.setStoreLocation(address);

        Store store = Store.builder()
                .name(dto.getStoreName())
                .businessRegistrationNumber(dto.getBusinessRegistrationNumber())
                .phone(dto.getStorePhone())
                .status(dto.getStoreStatus())
                .type(dto.getStoreType())
                .location(dto.getStoreLocation())
                .contractStartDate(dto.getStoreContractStartDate())
                .contractAffiliateDate(dto.getStoreContractAffiliateDate())
                .contractTerm(dto.getStoreContractTerm())
                .affiliatePrice(dto.getStoreAffiliatePrice())
                .monthlySales(dto.getStoreMonthlySales())
                .royalty(dto.getRoyalty())
                .comment(dto.getComment())
                .build();

        Store saved = storeRepository.save(store);
        Long id = saved.getId();

        return id;
    }

    /**
     * 가맹점의 이름(및 필요 시 식별자 등 최소 필드)을 DTO로 조회한다.
     *
     * @return 가맹점 표시용 DTO 리스트. 데이터가 없으면 일반적으로 빈 리스트를 반환.
     * @see com.boot.ict05_final_admin.domain.store.repository.StoreRepository#findStoreName()
     */
    public List<FindStoreDTO> findStoreName() {
        return storeRepository.findStoreName();
    }

    /**
     * 가맹점 상세 정보를 조회한다.
     *
     * @param id 가맹점 ID
     * @return 가맹점 엔티티, 존재하지 않으면 null
     */
    public Store detailOfficeStore(Long id) { return storeRepository.findById(id).orElse(null); }
    }



