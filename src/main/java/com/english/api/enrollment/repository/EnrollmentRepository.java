package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for Enrollment entity
 * Created by hungpham on 10/29/2025
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    
    /**
     * Check if enrollment exists for a user and course combination
     */
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
    
    /**
     * Find all course IDs that user is already enrolled in (batch check)
     * Optimized for batch operations to avoid N+1 queries
     */
    @Query("""
        SELECT e.course.id FROM Enrollment e 
        WHERE e.user.id = :userId 
        AND e.course.id IN :courseIds
        """)
    Set<UUID> findEnrolledCourseIds(@Param("userId") UUID userId, @Param("courseIds") List<UUID> courseIds);
    
    /**
     * Find all enrollments for a user with course information eagerly loaded
     * Ordered by enrollment creation date (newest first)
     * Only fetches minimal course fields to avoid loading heavy descriptions
     * Supports pagination
     */
    @Query("""
        SELECT e FROM Enrollment e 
        LEFT JOIN FETCH e.course 
        WHERE e.user.id = :userId 
        ORDER BY e.createdAt DESC
        """)
    Page<Enrollment> findByUserIdWithCourse(@Param("userId") UUID userId, Pageable pageable);
}
