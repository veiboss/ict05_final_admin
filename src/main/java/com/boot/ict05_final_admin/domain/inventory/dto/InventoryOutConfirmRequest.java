package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 출고 확정 요청 DTO
 *
 * <p>출고 확정 API 호출 시 사용되는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>materialId: 출고 대상 재료 ID</li>
 *   <li>storeId: 출고 대상 가맹점 ID(선택, 내부 사용/폐기 등은 null)</li>
 *   <li>totalQty: 총 출고 수량</li>
 *   <li>outDate: 출고일시(선택, 미전달 시 서비스에서 now 적용)</li>
 *   <li>memo: 비고(선택)</li>
 *   <li>allocation: 배치별 출고 수량 강제 지정 맵(선택). key=배치ID, value=수량.
 *       미전달 또는 빈 맵이면 서비스에서 FIFO 할당을 수행한다.</li>
 * </ul>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOutConfirmRequest {

    /** 출고 대상 재료 ID */
    private Long materialId;

    /** 출고 대상 가맹점 ID(선택, 내부 사용/폐기 등은 null) */
    private Long storeId;

    /** 총 출고 수량 */
    private BigDecimal totalQty;

    /** 출고일시(선택, 미전달 시 now 적용) */
    private LocalDateTime outDate;

    /** 비고(선택) */
    private String memo;

    /**
     * 배치별 출고 수량 강제 지정 맵(선택).
     * <p>key: 배치ID, value: 출고 수량</p>
     * <p>미전달 또는 비어 있으면 서비스에서 FIFO로 자동 배치 할당.</p>
     */
    private Map<Long, BigDecimal> allocation;
}
