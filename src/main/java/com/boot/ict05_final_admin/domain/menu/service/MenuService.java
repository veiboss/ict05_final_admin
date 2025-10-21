package com.boot.ict05_final_admin.domain.menu.service;

import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.repository.MenuAttachmentRepository;
import com.boot.ict05_final_admin.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional      // DB 작업은 하나의 트랜잭션 단위로 처리 - 중간에 에러 나면 모두 취소
@Slf4j  // 로그를 찍을 수 있음
public class MenuService {

    private final MenuRepository menuRepository;    // @RequiredArgsConstructor가 자동으로 주입해 줘서 @Autowired가 필요 없음
    private final MenuAttachmentRepository menuAttachmentRepository;
    private final MenuAttachmentService menuAttachmentService;

    /**
     * 작성자 이름으로 필터링하여 메뉴 목록을 페이지 단위로 조회한다.
     *
     * @param menuSearchDTO
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 메뉴 리스트 DTO
     */
    public Page<MenuListDTO> selectMenu(MenuSearchDTO menuSearchDTO, Pageable pageable) {
        return menuRepository.listMenu(menuSearchDTO, pageable);
    }

    /**
     * 새로운 메뉴를 등록하고 첨부파일을 저장한다.
     *
     * @param dto   메뉴 등록 정보 (제목, 내용, 카테고리 등)
     * @param files 첨부파일 리스트 (없을 수 있음)
     * @return 저장된 메뉴 ID
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    public Long insertStoreMenu(MenuListDTO menuListDTO, List<MultipartFile> files) throws Exception {
        Menu menu = Menu.builder()
                .menuCategory()
    }

}
