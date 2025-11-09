package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmPreferenceRepository extends JpaRepository<FcmPreference, Long> {

	Optional<FcmPreference> findFirstByAppTypeAndMemberIdFk(AppType appType, Long memberIdFk);
}