package com.english.api.course.repository;

import com.english.api.course.model.LessonBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/8/2025
 */
@Repository
public interface LessonBlockRepository extends JpaRepository<LessonBlock, UUID> {
    List<LessonBlock> findByLessonIdOrderByPosition(UUID lessonId);
    boolean existsByLessonIdAndPosition(UUID lessonId, Integer position);

    @Query("SELECT COALESCE(MAX(b.position), 0) FROM LessonBlock b WHERE b.lesson.id = :lessonId")
    Optional<Integer> findMaxPositionByLessonId(@Param("lessonId") UUID lessonId);
}


