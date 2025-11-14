package com.boot.ict05_final_admin.domain.staffresources.repository;

import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<StaffProfile, Long>, StaffRepositoryCustom {

    // @OneToOne private Member member; 라면, 컬럼명이 member_id_fk여도 프로퍼티 경로는 member.id
    Optional<StaffProfile> findByMember_Id(Long memberId);

    boolean existsByMember_Id(Long memberId);

    @EntityGraph(attributePaths = {"store"})
    @Query("select s from StaffProfile s where s.id = :id")
    Optional<StaffProfile> findWithStoreById(@Param("id") Long id);
}
