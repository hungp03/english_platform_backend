package com.english.api.course.repository;

/**
 * Created by hungpham on 10/10/2025
 */

import com.english.api.course.model.LessonMedia;
import com.english.api.course.model.enums.LessonMediaRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonMediaRepository extends JpaRepository<LessonMedia, UUID> {
    Optional<LessonMedia> findByLessonIdAndRole(UUID lessonId, LessonMediaRole role);
    boolean existsByLessonIdAndRole(UUID lessonId, LessonMediaRole role);
    void deleteByLessonIdAndMediaId(UUID lessonId, UUID mediaId);
    boolean existsByLessonIdAndMediaId(UUID lessonId, UUID mediaId);
    List<LessonMedia> findByLessonIdOrderByPositionAsc(UUID lessonId);
    Optional<LessonMedia> findByLessonIdAndMediaId(UUID lessonId, UUID assetId);
}

