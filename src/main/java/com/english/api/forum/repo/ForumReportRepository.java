// package com.english.api.forum.repo;
// import com.english.api.forum.entity.ForumReport;
// import com.english.api.forum.entity.ReportTargetType;

// import java.util.List;

// import org.springframework.data.domain.*;
// import org.springframework.data.jpa.repository.JpaRepository;
// public interface ForumReportRepository extends JpaRepository<ForumReport, java.util.UUID> {
//   Page<ForumReport> findByTargetTypeAndResolvedAtIsNull(ReportTargetType type, Pageable pageable);
//   Page<ForumReport> findByTargetType(ReportTargetType type, Pageable pageable);
//   Page<ForumReport> findAll(Pageable pageable);
//   List<ForumReport> findTop15ByResolvedAtIsNullOrderByCreatedAtAsc();
//   Long countByResolvedAtIsNull();
// }

package com.english.api.forum.repo;

import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ForumReportRepository extends JpaRepository<ForumReport, UUID> {

    // Load sẵn user và resolvedBy để tối ưu hiệu năng
    @EntityGraph(attributePaths = {"user", "resolvedBy"})
    Page<ForumReport> findByTargetTypeAndResolvedAtIsNull(ReportTargetType type, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resolvedBy"})
    Page<ForumReport> findByTargetType(ReportTargetType type, Pageable pageable);
    
    // Đếm số lượng report chưa giải quyết
    Long countByResolvedAtIsNull();
}