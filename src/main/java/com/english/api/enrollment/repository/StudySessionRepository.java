package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for StudySession entity
 * Created by hungpham on 10/29/2025
 */
@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {
}
