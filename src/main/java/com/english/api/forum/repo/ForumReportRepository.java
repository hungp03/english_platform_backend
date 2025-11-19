package com.english.api.forum.repo;

import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumReportRepository extends JpaRepository<ForumReport, java.util.UUID> {
    Page<ForumReport> findByTargetTypeAndResolvedAtIsNull(ReportTargetType type, Pageable pageable);

    Page<ForumReport> findByTargetType(ReportTargetType type, Pageable pageable);

    Page<ForumReport> findAll(Pageable pageable);
}
