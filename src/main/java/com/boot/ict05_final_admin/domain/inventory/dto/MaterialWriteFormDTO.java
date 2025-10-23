package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 재료 등록 폼 DTO
 *
 * <p>재료 신규 등록 시 클라이언트에서 전달되는 데이터 전송 객체(DTO)이다.</p>
 */
@Data
public class MaterialWriteFormDTO {

    /** 작성자(회원) FK */
    private Long memberIdFk;

    /** 재료 CODE */
    private String code;

    /** 재료 카테고리 */
    private MaterialCategory materialCategory;

    /** 재료명 (필수) */
    @NotBlank(message = "재료명을 입력해주세요")
    private String name;

    /** 기본 단위 (필수) */
    @NotBlank(message = "기본 단위를 입력해주세요")
    private String baseUnit;

    /** 판매 단위 (필수) */
    @NotBlank(message = "판매 단위를 입력해주세요")
    private String salesUnit;

    /** 판매단위 → 기본단위 변환비율 (필수) */
    @NotNull(message = "변환비율을 입력해주세요")
    private Double conversionRate;

    /** 공급업체명 */
    private String supplier;

    /** 보관 온도 (필수) */
    @NotNull(message = "보관 온도를 선택해주세요")
    private MaterialTemperature materialTemperature;
}
