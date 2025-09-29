package com.boot.ict05_final_admin.domain.notice.repository;

import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface NoriceRepository extends JpaRepository<Notice, Long> {
}
