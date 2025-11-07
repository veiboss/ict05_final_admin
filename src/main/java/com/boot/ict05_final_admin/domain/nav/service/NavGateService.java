package com.boot.ict05_final_admin.domain.nav.service;

import com.boot.ict05_final_admin.domain.nav.dto.NavListDTO;
import com.boot.ict05_final_admin.domain.nav.dto.NavSearchDTO;
import com.boot.ict05_final_admin.domain.nav.entity.NavItem;
import com.boot.ict05_final_admin.domain.nav.repository.NavItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NavGateService {

    private final NavItemRepository navItemRepository;

    /** 요청 URI가 nav에서 비활성인지 판단. nav에 없으면 통과(true) */
    public boolean isEnabledPath(String requestUri) {
        List<NavItem> all = navItemRepository.findAll();
        Optional<NavItem> match = all.stream()
                .filter(n -> requestUri.startsWith(n.getNavItemPath()))
                .max(Comparator.comparingInt(n -> n.getNavItemPath().length()));

        // nav로 관리하지 않는 경로면 여기서는 통과 → 최종 보안은 Security가 담당
        return match.map(NavItem::isNavItemEnabled).orElse(true);
    }

    /**
     * 검색어로 필터링하여 시스템 메뉴 목록을 페이지 단위로 조회한다.
     *
     * @param navSearchDTO 검색 (선택, null 가능)
     * @param pageable       페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 시스템 메뉴 리스트 DTO
     */
    public Page<NavListDTO> selectAllNav(NavSearchDTO navSearchDTO, Pageable pageable) {
        return navItemRepository.listNav(navSearchDTO, pageable);
    }

    @Transactional
    public boolean setEnabled(Long id, boolean enabled) {
        int cnt = navItemRepository.updateEnabled(id, enabled);
        if (cnt == 0) throw new IllegalArgumentException("대상 메뉴가 없습니다: " + id);
        return enabled;
    }

    @Transactional
    public boolean toggle(Long id) {
        NavItem nav = navItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대상 메뉴가 없습니다: " + id));
        boolean next = !nav.isNavItemEnabled();
        nav.setNavItemEnabled(next); // dirty checking
        return next;
    }
}
