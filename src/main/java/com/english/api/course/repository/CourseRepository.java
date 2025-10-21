package com.english.api.course.repository;

import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseWithStatsResponse;
import com.english.api.course.model.Course;
import com.english.api.course.repository.custom.CourseRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, CourseRepositoryCustom {
    boolean existsBySlug(String slug);

    @Query("""
        SELECT new com.english.api.course.dto.response.CourseDetailResponse(
            c.id,
            c.title,
            c.slug,
            c.description,
            c.detailedDescription,
            c.language,
            c.thumbnail,
            c.skillFocus,
            c.priceCents,
            c.currency,
            c.status,
            cb.fullName,
            c.updatedAt,
            COALESCE((SELECT COUNT(m) FROM CourseModule m WHERE m.course.id = c.id), 0L),
            COALESCE((SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = c.id), 0L)
        )
        FROM Course c
        LEFT JOIN c.createdBy cb
        WHERE c.id = :id
    """)
    Optional<CourseDetailResponse> findDetailById(@Param("id") UUID id);

    @Query("""
        SELECT new com.english.api.course.dto.response.CourseDetailResponse(
            c.id,
            c.title,
            c.slug,
            c.description,
            c.detailedDescription,
            c.language,
            c.thumbnail,
            c.skillFocus,
            c.priceCents,
            c.currency,
            c.status,
            cb.fullName,
            c.updatedAt,
            COALESCE((SELECT COUNT(m) FROM CourseModule m WHERE m.course.id = c.id AND m.published = true), 0L),
            COALESCE((SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = c.id AND l.published = true), 0L)
        )
        FROM Course c
        LEFT JOIN c.createdBy cb
        WHERE c.slug = :slug AND c.status = com.english.api.course.model.enums.CourseStatus.PUBLISHED
    """)
    Optional<CourseDetailResponse> findDetailBySlug(@Param("slug") String slug);

    
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
}
