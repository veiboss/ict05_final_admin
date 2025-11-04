package com.boot.ict05_final_admin.domain.position.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.member.service.MemberService;
import com.boot.ict05_final_admin.domain.position.repository.PositionRepository;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class PositionService {

    private final MemberService memberService;
    private final StaffService staffService;
    private final PositionRepository positionRepository;

    public StaffProfile memberLinkStaff(Long memberId) {

        Member member = memberService.findById(memberId);
        if (member == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버가 존재하지 않습니다");

        Long staffId = positionRepository.searchStaffEmail(member.getEmail());
        if (staffId == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "동일 이메일의 사원이 없습니다");

        StaffProfile staff = staffService.findById(staffId);
        if (staff == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사원 정보가 존재하지 않습니다");

        // 이미 다른 멤버와 연결된 경우
        if (staff.getMember() != null && !staff.getMember().getId().equals(memberId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 사원은 이미 다른 멤버와 연결되어 있습니다");

        staff.changeMember(member); // 영속 상태라 트랜잭션 종료 시 flush
        return staff;
    }
}
