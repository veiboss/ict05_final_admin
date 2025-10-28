package com.boot.ict05_final_admin.domain.myPage.dto;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import lombok.*;

/**
 * 마이페이지 조회용 DTO
 *
 * <p>회원의 주요 정보를 전달하기 위한 읽기 전용 DTO입니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageDTO {

    /** 회원 ID */
    private Long id;

    /** 회원 이름 */
    private String name;

    /** 회원 이메일 */
    private String email;

    /** 회원 전화번호 */
    private String phone;

    /**
     * Entity → DTO 변환 메서드
     */
    public static MyPageDTO fromEntity(Member member) {
        return MyPageDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .build();
    }
}
