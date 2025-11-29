package com.english.api.forum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.english.api.forum.model.ForumReport;
import com.english.api.forum.model.ReportTargetType;

import java.util.List;
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

    Page<ForumReport> findAll(Pageable pageable);

    @Modifying
    @Query("DELETE FROM ForumReport r WHERE r.targetId = :targetId")
    void deleteByTargetId(@Param("targetId") UUID targetId);

    @Modifying
    @Query("DELETE FROM ForumReport r WHERE r.targetId IN :targetIds")
    void deleteByTargetIds(@Param("targetIds") List<UUID> targetIds);
}
