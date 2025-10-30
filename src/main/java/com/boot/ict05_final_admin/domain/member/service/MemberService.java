package com.boot.ict05_final_admin.domain.member.service;

import com.boot.ict05_final_admin.domain.member.dto.MemberListDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberSearchDTO;
import com.boot.ict05_final_admin.domain.member.repository.MemberRepository;
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
public class MemberService {

    private final MemberRepository memberRepository;

    public Page<MemberListDTO> selectAllMember(MemberSearchDTO memberSearchDTO, Pageable pageable) {
        return memberRepository.listMember(memberSearchDTO, pageable);
    }
}
