package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for LessonProgress entity
 * Created by hungpham on 10/29/2025
 */
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {
}
