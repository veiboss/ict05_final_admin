package com.boot.ict05_final_admin.domain.store.entity;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.StoreStatus;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.StoreType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가맹점(Store) 엔티티 클래스
 *
 * <p>본 클래스는 공지사항 테이블과 매핑되며,
 * 가맹점의 매장명,</p>
 * */

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store")
public class Store {

    /** 매장 고유 Id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id ")
    private Long storeId;

    /** 매장 점주 시퀀스 */
    @Column(name = "staff_id_fk", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long staffId;  // FK 후보 - 점주

    /** 본사 담당자 시퀀스 */
    @Column(name = "member_id_fk", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long memberId;  // FK 후보 - 본사 담당자

    /** 매장명 */
    @Column(name = "store_name")
    private String storeName;

    /** 가맹점 주소 */
    @Column(name = "store_location", length = 255)
    private String location;

    /** 가맹점 구분 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", length = 10)
    private StoreType type = StoreType.FRANCHISE;

    /** 가맹점 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", length = 10)
    private StoreStatus status = StoreStatus.OPERATING;

    /** 가맹점 계약 시작일 */
    @Column(name = "store_contract_start_date")
    private LocalDate contractStartDate;

    /** 가맹점 계약 가맹일 */
    @Column(name = "store_contract_affiliate_date")
    private LocalDate contractAffiliateDate;

    /** 가맹점 계약 기간 */
    @Column(name = "store_contract_term")
    private Integer contractTerm;

    /** 가맹점 가맹비 */
    @Column(name = "store_affiliate_price", precision = 14, scale = 2)
    private BigDecimal affiliatePrice;

    /** 가맹점 월매출 */
    @Column(name = "store_monthly_sales", precision = 14, scale = 2)
    private BigDecimal monthlySales;

    /** 가맹점 연락처 */
    @Column(name = "store_phone", length = 50)
    private String phone;

    /** 가맹점 사업 등록번호 */
    @Column(name = "business_registration_number", length = 50)
    private String businessRegistrationNumber;

    /** 가맹점 특이사항 */
    @Column(name = "store_comment", columnDefinition = "TEXT")
    private String comment;

    /** 가맹점 로열티 */
    @Column(name = "store_royalty", precision = 8, scale = 4)
    private BigDecimal royalty;


    private int storeTotalEmployees;
}
