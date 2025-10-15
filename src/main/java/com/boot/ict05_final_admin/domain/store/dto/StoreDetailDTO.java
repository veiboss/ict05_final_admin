package com.boot.ict05_final_admin.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 가맹점 상세 정보 DTO
 *
 * <p>가맹점 상세 조회 시 사용되는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *
 * </ul>
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreDetailDTO {

    /** 매장 고유 Id */
    private Long storeId;

}
