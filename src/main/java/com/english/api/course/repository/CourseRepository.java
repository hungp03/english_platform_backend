package com.english.api.course.repository;

import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseWithStatsResponse;
import com.english.api.course.model.Course;
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
public interface CourseRepository extends JpaRepository<Course, UUID> {
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
            c.published,
            cb.fullName,
            c.updatedAt,
            COUNT(DISTINCT m.id),
            COUNT(DISTINCT l.id)
        )
        FROM Course c
        LEFT JOIN c.createdBy cb
        LEFT JOIN CourseModule m ON m.course.id = c.id
        LEFT JOIN Lesson l ON l.module.id = m.id
        WHERE c.id = :id
        GROUP BY c.id, cb.fullName
    """)
    Optional<CourseDetailResponse> findDetailById(@Param("id") UUID id);

    @Query("""
            SELECT new com.english.api.course.dto.response.CourseWithStatsResponse(
                c.id,
                c.title,
                c.description,
                c.language,
                c.thumbnail,
                c.skillFocus,
                c.priceCents,
                c.currency,
                c.published,
                COUNT(DISTINCT m.id),
                COUNT(DISTINCT l.id),
                c.createdAt,
                c.updatedAt
            )
            FROM Course c
            LEFT JOIN CourseModule m ON m.course.id = c.id
            LEFT JOIN Lesson l ON l.module.id = m.id
            GROUP BY c.id, c.title, c.description, c.language,c.thumbnail, c.skillFocus,
                     c.priceCents, c.currency, c.published, c.createdAt, c.updatedAt
            """)
    Page<CourseWithStatsResponse> findAllWithStats(Pageable pageable);

    @Query("""
            SELECT new com.english.api.course.dto.response.CourseWithStatsResponse(
                c.id,
                c.title,
                c.description,
                c.language,
                c.thumbnail,
                c.skillFocus,
                c.priceCents,
                c.currency,
                c.published,
                COUNT(DISTINCT m.id),
                COUNT(DISTINCT l.id),
                c.createdAt,
                c.updatedAt
            )
            FROM Course c
            LEFT JOIN CourseModule m ON m.course.id = c.id
            LEFT JOIN Lesson l ON l.module.id = m.id
            WHERE (:keyword IS NULL OR 
                   LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isPublished IS NULL OR c.published = :isPublished)
            GROUP BY c.id, c.title, c.description, c.language,c.thumbnail, c.skillFocus,
                     c.priceCents, c.currency, c.published, c.createdAt, c.updatedAt
            """)
    Page<CourseWithStatsResponse> searchWithStats(
            @Param("keyword") String keyword,
            @Param("isPublished") Boolean isPublished,
            Pageable pageable
    );

    @Query("""
                SELECT new com.english.api.course.dto.response.CourseWithStatsResponse(
                    c.id,
                    c.title,
                    c.description,
                    c.language,
                    c.thumbnail,
                    c.skillFocus,
                    c.priceCents,
                    c.currency,
                    c.published,
                    COUNT(DISTINCT m.id) AS moduleCount,
                    COUNT(DISTINCT l.id) AS lessonCount,
                    c.createdAt,
                    c.updatedAt
                )
                FROM Course c
                LEFT JOIN CourseModule m ON m.course.id = c.id
                LEFT JOIN Lesson l ON l.module.id = m.id
                WHERE c.createdBy.id = :ownerId
                  AND c.deleted = false
                  AND (
                      :keyword IS NULL
                      OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
                      OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
                  )
                  AND (:isPublished IS NULL OR c.published = :isPublished)
                GROUP BY c.id, c.title, c.description, c.language,c.thumbnail, c.skillFocus,
                         c.priceCents, c.currency, c.published, c.createdAt, c.updatedAt
            """)
    Page<CourseWithStatsResponse> searchByOwnerWithStats(
            @Param("ownerId") UUID ownerId,
            @Param("keyword") String keyword,
            @Param("isPublished") Boolean isPublished,
            Pageable pageable
    );

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
