package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LessonProgress entity
 * Created by hungpham on 10/29/2025
 */
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {
    Optional<LessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    @Modifying
    @Query("UPDATE LessonProgress lp SET lp.completed = NOT lp.completed, lp.lastSeenAt = CURRENT_TIMESTAMP " +
           "WHERE lp.user.id = :userId AND lp.lesson.id = :lessonId")
    int toggleCompleted(@Param("userId") UUID userId, @Param("lessonId") UUID lessonId);

    @Query("""
        SELECT COUNT(lp) FROM LessonProgress lp
        WHERE lp.user.id = :userId
        AND lp.lesson.module.course.id = :courseId
        AND lp.completed = true
        """)
    long countCompletedLessonsByUserAndCourse(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("""
        SELECT lp.lesson.id FROM LessonProgress lp
        WHERE lp.user.id = :userId
        AND lp.lesson.module.course.id = :courseId
        AND lp.completed = true
        ORDER BY lp.lastSeenAt DESC
        LIMIT 1
        """)
    Optional<UUID> findLastCompletedLessonIdByUserAndCourse(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
}
