package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 단일 로트의 출고 이력 행 DTO
 *
 * <p>배치(로트) 기준 출고 이력 조회 시 사용되는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>outId: 출고 헤더 ID</li>
 *   <li>storeId: 가맹점 ID(내부 사용/폐기 등일 경우 null 가능)</li>
 *   <li>storeName: 가맹점 명칭(선택 필드, 성능을 위해 미조인 가능)</li>
 *   <li>qty: 해당 로트에서 차감된 출고 수량</li>
 *   <li>outDate: 출고 일시</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BatchOutRowDTO {

    /** 출고 헤더 ID */
    private Long outId;

    /** 가맹점 ID(내부 사용/폐기 등은 null 가능) */
    private Long storeId;

    /** 가맹점 명칭(선택) */
    private String storeName;

    /** 로트 단위 출고 수량 */
    private BigDecimal qty;

    /** 출고 일시 */
    private LocalDateTime outDate;
}
