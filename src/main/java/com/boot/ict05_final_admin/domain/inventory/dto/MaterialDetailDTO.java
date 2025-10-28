package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재료 상세 정보 DTO
 *
 * <p>재료 상세 조회 시 사용되는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li>id: 재료 고유 ID</li>
 *     <li>code: 재료 코드</li>
 *     <li>title: 재료명</li>
 *     <li>materialCategory: 재료 카테고리</li>
 *     <li>baseUnit: 기본 단위</li>
 *     <li>salesUnit: 판매 단위</li>
 *     <li>conversionRate: 단위 변환비율</li>
 *     <li>supplier: 공급업체명</li>
 *     <li>materialTemperature: 보관 온도</li>
 *     <li>materialStatus: 상태</li>
 *     <li>createdAt: 등록일시</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MaterialDetailDTO {

    /** 재료 고유 ID */
    private Long id;

    /** 재료 코드 */
    private String code;

    /** 재료명 */
    private String title;

    /** 재료 카테고리 */
    private MaterialCategory materialCategory;

    /** 기본 단위 */
    private String baseUnit;

    /** 판매 단위 */
    private String salesUnit;

    /** 단위 변환비율 */
    private Integer conversionRate;

    /** 공급업체명 */
    private String supplier;

    /** 보관 온도 */
    private MaterialTemperature materialTemperature;

    /** 상태 */
    private MaterialStatus materialStatus;
}
