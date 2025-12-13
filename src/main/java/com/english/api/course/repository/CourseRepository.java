package com.english.api.course.repository;

import com.english.api.course.dto.response.CourseCheckoutResponse;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.model.Course;
import com.english.api.course.model.enums.CourseStatus;
import com.english.api.course.repository.custom.CourseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, CourseRepositoryCustom {
    boolean existsBySlug(String slug);

    @Query(value = """
        SELECT c.id, c.title, c.slug, c.description, c.detailed_description, 
               c.language, c.thumbnail, 
               COALESCE(ARRAY_AGG(DISTINCT s.name) FILTER (WHERE s.name IS NOT NULL), ARRAY[]::text[]) as skills,
               c.price_cents, c.currency, c.status, 
               cb.id as instructor_id, cb.full_name as created_by, c.updated_at,
               COALESCE((SELECT COUNT(*) FROM course_modules m WHERE m.course_id = c.id), 0) as module_count,
               COALESCE((SELECT COUNT(*) FROM course_modules m 
                        INNER JOIN lessons l ON l.module_id = m.id 
                        WHERE m.course_id = c.id), 0) as lesson_count,
               COALESCE((SELECT COUNT(*) FROM enrollments e 
                        WHERE e.course_id = c.id AND e.status = 'ACTIVE'), 0) as student_count,
               COALESCE((SELECT AVG(r.rating) FROM course_reviews r 
                        WHERE r.course_id = c.id AND r.is_published = true), 0.0) as average_rating,
               COALESCE((SELECT COUNT(*) FROM course_reviews r 
                        WHERE r.course_id = c.id AND r.is_published = true), 0) as total_reviews
        FROM courses c
        LEFT JOIN users cb ON c.created_by = cb.id
        LEFT JOIN course_skills cs ON c.id = cs.course_id
        LEFT JOIN skills s ON cs.skill_id = s.id
        WHERE c.id = :id
        GROUP BY c.id, c.title, c.slug, c.description, c.detailed_description, 
                 c.language, c.thumbnail, c.price_cents, c.currency, c.status, 
                 cb.id, cb.full_name, c.updated_at
    """, nativeQuery = true)
    List<Object[]> findDetailByIdNative(@Param("id") UUID id);

    @Query(value = """
        SELECT c.id, c.title, c.slug, c.description, c.detailed_description, 
               c.language, c.thumbnail, 
               COALESCE(ARRAY_AGG(DISTINCT s.name) FILTER (WHERE s.name IS NOT NULL), ARRAY[]::text[]) as skills,
               c.price_cents, c.currency, c.status, 
               cb.id as instructor_id, cb.full_name as created_by, c.updated_at,
               COALESCE((SELECT COUNT(*) FROM course_modules m WHERE m.course_id = c.id AND m.published = true), 0) as module_count,
               COALESCE((SELECT COUNT(*) FROM course_modules m 
                        INNER JOIN lessons l ON l.module_id = m.id 
                        WHERE m.course_id = c.id AND l.published = true), 0) as lesson_count,
               COALESCE((SELECT COUNT(*) FROM enrollments e 
                        WHERE e.course_id = c.id AND e.status = 'ACTIVE'), 0) as student_count,
               COALESCE((SELECT AVG(r.rating) FROM course_reviews r 
                        WHERE r.course_id = c.id AND r.is_published = true), 0.0) as average_rating,
               COALESCE((SELECT COUNT(*) FROM course_reviews r 
                        WHERE r.course_id = c.id AND r.is_published = true), 0) as total_reviews
        FROM courses c
        LEFT JOIN users cb ON c.created_by = cb.id
        LEFT JOIN course_skills cs ON c.id = cs.course_id
        LEFT JOIN skills s ON cs.skill_id = s.id
        WHERE c.slug = :slug AND c.status = 'PUBLISHED'
        GROUP BY c.id, c.title, c.slug, c.description, c.detailed_description, 
                 c.language, c.thumbnail, c.price_cents, c.currency, c.status, 
                 cb.id, cb.full_name, c.updated_at
    """, nativeQuery = true)
    List<Object[]> findDetailBySlugNative(@Param("slug") String slug);

    
    @Query("SELECT c.createdBy.id FROM Course c WHERE c.id = :id")
    Optional<UUID> findOwnerIdById(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Course c SET c.deleted = true, c.deletedAt = :now WHERE c.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("now") Instant now);

    @Query("""
            SELECT c.createdBy.id
            FROM Course c
            JOIN CourseModule m ON m.course.id = c.id
            JOIN Lesson l ON l.module.id = m.id
            WHERE l.id = :lessonId
            """)
    Optional<UUID> findOwnerIdByLessonId(@Param("lessonId") UUID lessonId);

    /**
     * Gets minimal course information needed for checkout payment display.
     * Only returns essential fields to minimize data transfer and avoid loading unnecessary data.
     *
     * @param id the course identifier
     * @return minimal course information for checkout
     */
    @Query("""
        SELECT new com.english.api.course.dto.response.CourseCheckoutResponse(
            c.id,
            c.title,
            c.thumbnail,
            c.priceCents,
            c.currency
        )
        FROM Course c
        WHERE c.id = :id AND c.status = com.english.api.course.model.enums.CourseStatus.PUBLISHED
    """)
    Optional<CourseCheckoutResponse> findCheckoutInfoById(@Param("id") UUID id);

    /**
     * Checks if a published course exists with the given ID
     * Used for order validation to ensure only published courses can be ordered
     *
     * @param id the course identifier
     * @return true if a published course exists with the given ID, false otherwise
     */
    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.id = :id AND c.status = com.english.api.course.model.enums.CourseStatus.PUBLISHED")
    boolean existsByIdAndStatusPublished(@Param("id") UUID id);
    
    /**
     * Get comprehensive statistics for an instructor using optimized PostgreSQL function
     * This includes: total courses, published courses, total students, and total revenue
     * 
     * @param instructorId the instructor's user ID
     * @return Object array with [totalCourses, publishedCourses, totalStudents, totalRevenueCents]
     */
    @Query(value = "SELECT * FROM get_instructor_stats(CAST(:instructorId AS uuid))", nativeQuery = true)
    Object[] getInstructorStats(@Param("instructorId") UUID instructorId);
    
    /**
     * Get revenue for an instructor within a specific date range
     * Only includes revenue from PAID orders for courses owned by the instructor
     * 
     * @param instructorId the instructor's user ID
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return total revenue in cents
     */
    @Query("""
        SELECT COALESCE(SUM(oi.unitPriceCents * oi.quantity - COALESCE(oi.discountCents, 0L)), 0L)
        FROM Order o
        JOIN o.items oi
        JOIN oi.course c
        WHERE c.createdBy.id = :instructorId
        AND o.status = 'PAID'
        AND o.paidAt >= :startDate
        AND o.paidAt <= :endDate
    """)
    Long getRevenueByInstructorAndDateRange(
        @Param("instructorId") UUID instructorId,
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate
    );
    
    /**
     * Get count of new students enrolled in instructor's courses within a specific date range
     * Only includes ACTIVE enrollments
     * 
     * @param instructorId the instructor's user ID
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return count of new students
     */
    @Query("""
        SELECT COUNT(DISTINCT e.user.id)
        FROM Enrollment e
        JOIN e.course c
        WHERE c.createdBy.id = :instructorId
        AND e.status = 'ACTIVE'
        AND e.createdAt >= :startDate
        AND e.createdAt <= :endDate
    """)
    Long getStudentCountByInstructorAndDateRange(
        @Param("instructorId") UUID instructorId,
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate
    );
    
    /**
     * Get monthly growth statistics using optimized PostgreSQL function
     */
    @Query(value = "SELECT * FROM get_monthly_growth(CAST(:instructorId AS uuid), :year, :month)", nativeQuery = true)
    List<Object[]> getMonthlyGrowth(
        @Param("instructorId") UUID instructorId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );

    Long countByStatus(CourseStatus status);
}
