package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 재료 수정 폼 DTO.
 *
 * <p>재료 수정 화면(Form)에서 서버로 전달되는 변경 파라미터 컨테이너.</p>
 *
 * <p>검증/정책:</p>
 * <ul>
 *   <li>{@code name}, {@code baseUnit}, {@code salesUnit}: 공백 불가</li>
 *   <li>{@code materialCategory}, {@code conversionRate}, {@code materialStatus}, {@code optimalQuantity}: 필수</li>
 *   <li>{@code optimalQuantity}: DECIMAL(15,3) 스케일 가정(서버/DB 정책에 맞춰 반올림)</li>
 *   <li>{@code materialTemperature}, {@code supplier}: 선택</li>
 * </ul>
 */
@Data
public class MaterialModifyFormDTO {

    /** 작성자(회원) FK (감사/감사로그용, 선택) */
    private Long memberIdFk;

    /** 수정 대상 재료 ID */
    private Long id;

    /** 재료명(필수) */
    @NotBlank(message = "재료명을 입력해주세요")
    private String name;

    /** 재료 카테고리(필수) */
    @NotNull(message = "카테고리를 선택해주세요")
    private MaterialCategory materialCategory;

    /** 기본 단위(소진 단위, 필수) */
    @NotBlank(message = "기본 단위를 입력해주세요")
    private String baseUnit;

    /** 판매 단위(필수) */
    @NotBlank(message = "판매 단위를 입력해주세요")
    private String salesUnit;

    /** 판매단위 → 기본단위 변환비율(필수, 예: 1 BOX → 20 EA = 20) */
    @NotNull(message = "변환비율을 입력해주세요")
    private Integer conversionRate;

    /** 공급업체명(선택) */
    private String supplier;

    /** 보관 온도 구분(선택) */
    private MaterialTemperature materialTemperature;

    /** 재료 상태(필수) */
    @NotNull(message = "상태를 선택해주세요")
    private MaterialStatus materialStatus;

    /** 본사 기준 적정 재고 수량(필수, DECIMAL(15,3) 가정) */
    @NotNull(message = "적정 수량을 입력해주세요")
    private BigDecimal optimalQuantity;
}
