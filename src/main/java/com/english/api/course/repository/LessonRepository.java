package com.english.api.course.repository;

import com.english.api.course.dto.response.LessonSummaryResponse;
import com.english.api.course.model.Lesson;
import com.english.api.enrollment.dto.response.LessonWithProgressResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    boolean existsByModuleIdAndPosition(UUID moduleId, Integer position);
    @Query("SELECT COALESCE(MAX(l.position), 0) FROM Lesson l WHERE l.module.id = :moduleId")
    Optional<Integer> findMaxPositionByModuleId(@Param("moduleId") UUID moduleId);

    @Query("""
        SELECT new com.english.api.course.dto.response.LessonSummaryResponse(
            l.id, l.title, l.kind, l.estimatedMin, l.position, l.isFree, l.published)
        FROM Lesson l
        WHERE l.module.id = :moduleId
        ORDER BY l.position
        """)
    List<LessonSummaryResponse> findSummaryByModuleId(@Param("moduleId") UUID moduleId);

    @Query("""
        SELECT new com.english.api.course.dto.response.LessonSummaryResponse(
            l.id, l.title, l.kind, l.estimatedMin, l.position, l.isFree, l.published)
        FROM Lesson l
        WHERE l.module.id = :moduleId AND l.published = true
        ORDER BY l.position
        """)
    List<LessonSummaryResponse> findPublishedSummaryByModuleId(@Param("moduleId") UUID moduleId);

    @Query("SELECT l.module.course.id FROM Lesson l WHERE l.id = :lessonId")
    Optional<UUID> findCourseIdByLessonId(@Param("lessonId") UUID lessonId);

    @Query("""
        SELECT COUNT(l) FROM Lesson l
        WHERE l.module.course.id = :courseId
        AND l.published = true
        """)
    long countPublishedLessonsByCourseId(@Param("courseId") UUID courseId);

    @Query("""
        SELECT new com.english.api.enrollment.dto.response.LessonWithProgressResponse(
            l.id, l.module.id, l.title, l.kind, l.estimatedMin, l.position, l.isFree, l.published,
            CASE WHEN lp.completed = true THEN true ELSE false END)
        FROM Lesson l
        LEFT JOIN LessonProgress lp ON lp.lesson.id = l.id AND lp.user.id = :userId
        WHERE l.module.id = :moduleId AND l.published = true
        ORDER BY l.position
        """)
    List<LessonWithProgressResponse> findPublishedLessonsWithProgress(@Param("moduleId") UUID moduleId, @Param("userId") UUID userId);

    @Query("""
        SELECT new com.english.api.enrollment.dto.response.LessonWithProgressResponse(
            l.id, l.module.id, l.title, l.kind, l.estimatedMin, l.position, l.isFree, l.published,
            CASE WHEN lp.completed = true THEN true ELSE false END)
        FROM Lesson l
        LEFT JOIN LessonProgress lp ON lp.lesson.id = l.id AND lp.user.id = :userId
        WHERE l.module.course.id = :courseId AND l.module.published = true AND l.published = true
        ORDER BY l.module.position, l.position
        """)
    List<LessonWithProgressResponse> findPublishedLessonsWithProgressByCourseId(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    Long countByIsFree(boolean isFree);
}
