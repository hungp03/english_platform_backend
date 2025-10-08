package com.english.api.course.repository;

import com.english.api.course.model.Lesson;
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
    List<Lesson> findByModuleIdOrderByPosition(UUID moduleId);
    boolean existsByModuleIdAndPosition(UUID moduleId, Integer position);
    @Query("SELECT COALESCE(MAX(l.position), 0) FROM Lesson l WHERE l.module.id = :moduleId")
    Optional<Integer> findMaxPositionByModuleId(@Param("moduleId") UUID moduleId);
}
