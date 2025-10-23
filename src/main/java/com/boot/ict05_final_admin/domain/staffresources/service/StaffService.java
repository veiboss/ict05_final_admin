package com.boot.ict05_final_admin.domain.staffresources.service;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffAddFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffListDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.repository.StaffRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
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
public class StaffService {

    private final StaffRepository staffRepository;
    private final StoreRepository storeRepository;

    /**
     * 검색어로 필터링하여 공지사항 목록을 페이지 단위로 조회한다.
     *
     * @param staffSearchDTO   검색 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 직원 리스트 DTO
     */
    public Page<StaffListDTO> selectAllStaff(StaffSearchDTO staffSearchDTO, Pageable pageable) {
        return staffRepository.listStaff(staffSearchDTO, pageable);
    }

    /**
     * 새로운 사원을 등록한다.
     *
     * @param dto   사원 등록 정보
     * @return 저장된 사원 ID
     */
    public long insertOfficeStaff(StaffAddFormDTO dto) {

        String address = "";
        String address1 = dto.getUserAddress1();
        String address2 = dto.getUserAddress2();
        address = address1 + "," + address2;
        dto.setStaffAddress(address);

        Store store = null;
        if (dto.getStoreIdFk() != null) {
            store = storeRepository.findById(dto.getStoreIdFk())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 근무지입니다."));
        }

        StaffProfile staff = StaffProfile.builder()
                .store(store)
                .staffName(dto.getStaffName())
                .staffEmploymentType(dto.getStaffEmploymentType())
                .staffDepartment(dto.getStaffDepartment())
                .staffEmail(dto.getStaffEmail())
                .staffPhone(dto.getStaffPhone())
                .staffAddress(dto.getStaffAddress())
                .staffBirth(dto.getStaffBirth())
                .staffStartDate(dto.getStaffStartDate())
                .staffEndDate(dto.getStaffEndDate())
                .build();

        StaffProfile saved = staffRepository.save(staff);
        Long id = saved.getId();

        return id;
    }

    /**
     * 사원 상세 정보를 조회한다.
     *
     * @param id 사원 ID
     * @return 사원 엔티티, 존재하지 않으면 null
     */
    public StaffProfile detailStaff(Long id) { return staffRepository.findById(id).orElse(null); }
}
