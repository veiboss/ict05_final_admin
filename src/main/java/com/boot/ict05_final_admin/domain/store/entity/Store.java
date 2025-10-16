package com.boot.ict05_final_admin.domain.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    /** 가맹점 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    /** 본사 담당자 시퀀스 */
    @Column(name = "member_id_fk", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long memberId;  // FK 후보 - 본사 담당자

    /** 가맹점명 */
    @Column(name = "store_name", length = 150, nullable = false)
    private String name;

    /** 가맹점 주소 */
    @Column(name = "store_location", length = 255)
    private String location;

    /** 가맹점 구분 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", length = 10)
    @Builder.Default
    private StoreType type = StoreType.FRANCHISE;

    /** 가맹점 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", length = 10)
    @Builder.Default
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


}
