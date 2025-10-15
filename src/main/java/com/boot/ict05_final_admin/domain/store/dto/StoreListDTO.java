package com.boot.ict05_final_admin.domain.store.dto;

import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 가맹점 목록 조회 DTO
 *
 * <p>가맹점 목록 조회 시 사용되는 데이터 전송 객체(DTO)이다.
 * 가맹점의 기본 정보를 포함한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li>storeId : 가맹점 고유 ID</li>
 *     <li>storeName : 가맹점 매장명</li>
 *     <li>storeStatus : 가맹점 상태</li>
 *     <li>storeOwnerName : 가맹점 점주명</li>
 *     <li>storePhone : 가맹점 연락처</li>
 *     <li>storeMonthlySales : 가맹점 월매출</li>
 *     <li>storeTotalEmployees : 가맹점 총 직원수</li>
 * </ul>
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreListDTO {

    /** 매장 고유 Id */
    private Long storeId;

    /** 매장명 */
    private String storeName;

    /** 매장 상태 */
    private StoreStatus storeStatus;

    /** 매장 연락처 */
    private String storePhone;

    /** 매장 월매출 */
    private BigDecimal storeMonthlySales;

    /** 매장 총 직원수 */
    private int storeTotalEmployees;

}
