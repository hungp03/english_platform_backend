package com.english.api.enrollment.repository;

import com.english.api.enrollment.dto.projection.EnrollmentProjection;
import com.english.api.enrollment.model.Enrollment;
import com.english.api.enrollment.model.enums.EnrollmentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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

    /**
     * Update progress percentage for an enrollment
     */
    @Modifying
    @Query("UPDATE Enrollment e SET e.progressPercent = :progressPercent WHERE e.user.id = :userId AND e.course.id = :courseId")
    int updateProgressPercent(@Param("userId") UUID userId, @Param("courseId") UUID courseId, @Param("progressPercent") BigDecimal progressPercent);

    /**
     * Find enrollment by user and course with course details eagerly loaded
     */
    @Query("""
        SELECT e FROM Enrollment e
        LEFT JOIN FETCH e.course
        WHERE e.user.id = :userId AND e.course.id = :courseId
        """)
    Optional<Enrollment> findByUserIdAndCourseIdWithCourse(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    /**
     * Find enrollment details by user and course slug (optimized projection)
     * Returns only the necessary fields without loading user entity
     */
    @Query("""
        SELECT e.id as enrollmentId, e.course.id as courseId, 
               e.course.title as courseTitle, e.progressPercent as progressPercent
        FROM Enrollment e
        WHERE e.user.id = :userId AND e.course.slug = :courseSlug
        """)
    Optional<EnrollmentProjection> findEnrollmentProjectionByUserIdAndCourseSlug(@Param("userId") UUID userId, @Param("courseSlug") String courseSlug);

    /**
     * Check if user is enrolled in the course that contains the given module
     * Optimized query using EXISTS for better performance (short-circuits on first match)
     */
    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1
            FROM Enrollment e
            WHERE e.user.id = :userId
              AND e.course.id = (
                  SELECT m.course.id FROM CourseModule m WHERE m.id = :moduleId
              )
        ) THEN true ELSE false END
        """)
    boolean isUserEnrolledInModuleCourse(@Param("userId") UUID userId, @Param("moduleId") UUID moduleId);

    /**
     * Check if user is enrolled in the course that contains the given lesson
     * Optimized query using EXISTS for better performance (short-circuits on first match)
     */
    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1
            FROM Enrollment e
            WHERE e.user.id = :userId
              AND e.course.id = (
                  SELECT l.module.course.id FROM Lesson l WHERE l.id = :lessonId
              )
        ) THEN true ELSE false END
        """)
    boolean isUserEnrolledInLessonCourse(@Param("userId") UUID userId, @Param("lessonId") UUID lessonId);

    Long countByStatus(EnrollmentStatus status);

    @Query("SELECT AVG(e.progressPercent) FROM Enrollment e WHERE e.status = 'ACTIVE'")
    BigDecimal calculateAverageProgress();

    // Get top courses by enrollment count
    @Query("SELECT e.course.id, COUNT(e) FROM Enrollment e GROUP BY e.course.id ORDER BY COUNT(e) DESC")
    List<Object[]> findTopCoursesByEnrollmentCount(Pageable pageable);

    // Get completion rate by course
    @Query("SELECT e.course.id, " +
        "COUNT(e) as total, " +
        "SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
        "AVG(e.progressPercent) as avgProgress " +
        "FROM Enrollment e " +
        "GROUP BY e.course.id")
    List<Object[]> getCourseCompletionStats();


    @Query("""
        SELECT 
            FUNCTION('DATE_TRUNC', 'month', e.startedAt) as month,
            COUNT(e) as newEnrollments,
            SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedEnrollments,
            COALESCE(AVG(e.progressPercent), 0) as avgProgress
        FROM Enrollment e 
        WHERE e.startedAt >= :startDate 
        GROUP BY FUNCTION('DATE_TRUNC', 'month', e.startedAt) 
        ORDER BY month DESC
        """)
    List<Object[]> getEnrollmentsByMonth(@Param("startDate") OffsetDateTime startDate);

    // @Query("""
    //     SELECT 
    //         c.id, c.title, c.slug, c.thumbnail,
    //         u.fullName, u.id,
    //         COUNT(e.id),
    //         SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END),
    //         AVG(r.rating)
    //     FROM Course c 
    //     JOIN c.enrollments e 
    //     JOIN c.createdBy u 
    //     WHERE c.status = 'PUBLISHED'
    //     GROUP BY c.id, c.title, c.slug, c.thumbnail, u.fullName, u.id
    //     ORDER BY COUNT(e.id) DESC
    //     """)
    // List<Object[]> findTopCoursesByEnrollmentCount(Pageable pageable);
}
