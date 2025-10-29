package com.boot.ict05_final_admin.domain.store.repository;

import com.boot.ict05_final_admin.domain.auth.entity.QMember;
import com.boot.ict05_final_admin.domain.staffresources.entity.QStaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffEmploymentType;
import com.boot.ict05_final_admin.domain.store.dto.FindStoreDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreDetailDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreListDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreSearchDTO;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 *  StoreRepositoryCustom 구현체(Querydsl 기반).
 *
 *  StoreRepositoryCustom의 “커스텀(사용자 정의) 쿼리”를 Querydsl로 구현한 클래스.
 * 즉, 기본 JpaRepository가 해주지 않는 동적 검색 + DTO 프로젝션 + 페이징/카운트를 처리하는 클래스.
 */

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    /**
     * 검색 조건 + 페이징으로 가맹점 목록을 조회한다.
     *
     * @param storeSearchDTO 검색 조건(키워드, 유형 등)
     * @param pageable       페이지/사이즈/정렬 정보(offset/limit에 사용)
     * @return Page<StoreListDTO> 페이징 결과
     */

    public Page<StoreListDTO> listStore(StoreSearchDTO storeSearchDTO, Pageable pageable) {
        QStore store = QStore.store;
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        // 1) 데이터 목록 조회 (DTO 프로젝션)
        List<StoreListDTO> content = queryFactory
                .select(Projections.fields(StoreListDTO.class,
                       store.id.as("storeId"),
                        store.name.as("storeName"),
                        store.status.as("storeStatus"),
                        ExpressionUtils.as(
                            JPAExpressions.select(staffProfile.staffName)
                                .from(staffProfile)
                                .where(
                                        staffProfile.store.id.eq(store.id),
                                        staffProfile.staffEmploymentType.eq(StaffEmploymentType.OWNER)
                                )
                                .limit(1),
                            "staffName"),
                        store.phone.as("storePhone"),
                        store.monthlySales.as("storeMonthlySales")
                        //store.storeTotalEmployees
                )) // member.name 매핑
                .from(store)
                .where(
                        eqSearchStore(storeSearchDTO, store),
                        eqStatus(storeSearchDTO, store) // ✅ 상태 필터 추가
                )
                .orderBy(store.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        // 2) 전체 카운트 조회 (동일 WHERE 적용)
        long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        eqSearchStore(storeSearchDTO, store),
                        eqStatus(storeSearchDTO, store) // ✅ 상태 필터 추가
                )
                .fetchOne();


        // 3) Page 구현체로 반환
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 단일 조건(매장명 like) 생성.
     * <p>검색어가 없으면 null을 반환하여 WHERE에서 무시되게 한다.</p>
     */
    private BooleanExpression eqSearchStore(StoreSearchDTO storeSearchDTO, QStore store) {
        if (storeSearchDTO.getKeyword() == null) {
            return null; // 조건 없음
        }

        String keyword = storeSearchDTO.getKeyword();

       return store.id.stringValue().containsIgnoreCase(keyword)
               .or(store.name.stringValue().containsIgnoreCase(keyword));
    }

    // ✅ 상태 필터
    private BooleanExpression eqStatus(StoreSearchDTO dto, QStore store) {
        return dto.getStatus() != null ? store.status.eq(dto.getStatus()) : null;
    }

    /**
     * 전체 카운트만 별도로 조회.
     * <p>페이징 total 계산 등에서 직접 호출할 때 사용.</p>
     */
    @Override
    public long countStore(StoreSearchDTO storeSearchDTO) {
        QStore store = QStore.store;

        long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        eqSearchStore(storeSearchDTO, store)
                )
                .fetchOne();

        return total;
    }

    @Override
    public List<FindStoreDTO> findStoreName() {
        QStore store = QStore.store;

        return queryFactory
                .select(Projections.fields(FindStoreDTO.class,
                    store.id.as("storeId"),
                    store.name.as("storeName")
                        )) // member.name 매핑
                .from(store)
                .orderBy(store.id.desc())
                .fetch();
    }

    @Override
    public StoreDetailDTO findByStoreDetail(Long id) {
        QStore store = QStore.store;
        QStaffProfile staffProfile = QStaffProfile.staffProfile;
        QMember member = QMember.member;

        return queryFactory
                .select(Projections.fields(StoreDetailDTO.class,
                        store.id.as("storeId"),
                        store.name.as("storeName"),
                        store.status.as("storeStatus"),
                        store.businessRegistrationNumber.as("businessRegistrationNumber"),
                        store.phone.as("storePhone"),
                        store.location.as("storeLocation"),
                        store.type.as("storeType"),
                        store.contractStartDate.as("storeContractStartDate"),
                        store.contractAffiliateDate.as("storeContractAffiliateDate"),
                        store.contractTerm.as("storeContractTerm"),
                        store.affiliatePrice.as("storeAffiliatePrice"),
                        store.monthlySales.as("storeMonthlySales"),
                        store.royalty.as("royalty"),
                        store.comment.as("comment"),

                        // ★ 명시적 LEFT JOIN으로 가져온 member 컬럼
                        member.name.as("memberName"),
                        member.email.as("memberEmail"),

                        // 점주명(OWNER 1명 가정) – 서브쿼리 유지
                        ExpressionUtils.as(
                                JPAExpressions.select(staffProfile.staffName)
                                        .from(staffProfile)
                                        .where(
                                                staffProfile.store.id.eq(store.id),
                                                staffProfile.staffEmploymentType.eq(StaffEmploymentType.OWNER)
                                        ),
                                "staffName"
                        ),

                        // 총 직원수 – 카운트 서브쿼리 (limit 불필요)
                        ExpressionUtils.as(
                                JPAExpressions.select(staffProfile.id.count())
                                        .from(staffProfile)
                                        .where(
                                                staffProfile.store.id.eq(store.id),
                                                staffProfile.staffEndDate.isNull()
                                        ),
                                "storeTotalEmployees"
                        )
                ))
                .from(store)
                // ★ 명시적 LEFT JOIN — member가 없더라도 null로 안전히 들어옴
                .leftJoin(store.member, member)
                .where(store.id.eq(id))
                .fetchOne();
    }

}
