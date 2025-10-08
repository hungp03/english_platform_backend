package com.english.api.course.repository;

import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.model.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {
    @Query("""
                SELECT new com.english.api.course.dto.response.CourseModuleResponse(
                    m.id, m.title, m.position, COUNT(l.id)
                )
                FROM CourseModule m
                LEFT JOIN Lesson l ON l.module.id = m.id
                WHERE m.course.id = :courseId
                GROUP BY m.id, m.title, m.position
                ORDER BY m.position
            """)
    List<CourseModuleResponse> findModulesWithLessonCount(UUID courseId);

    @Query("""
                SELECT new com.english.api.course.dto.response.CourseModuleResponse(
                    m.id, m.title, m.position, COUNT(l.id)
                )
                FROM CourseModule m
                LEFT JOIN Lesson l ON l.module.id = m.id
                WHERE m.course.id = :courseId AND m.id = :moduleId
                GROUP BY m.id, m.title, m.position
            """)
    Optional<CourseModuleResponse> findModuleWithLessonCount(UUID courseId, UUID moduleId);

    boolean existsByCourseIdAndPosition(UUID courseId, Integer position);

    @Query("SELECT COALESCE(MAX(m.position), 0) FROM CourseModule m WHERE m.course.id = :courseId")
    Optional<Integer> findMaxPositionByCourseId(@Param("courseId") UUID courseId);
}
