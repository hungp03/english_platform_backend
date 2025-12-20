package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.StudyPlanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface StudyPlanScheduleRepository extends JpaRepository<StudyPlanSchedule, UUID> {

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM StudyPlanSchedule s
        WHERE s.id = :scheduleId
        AND s.plan.id = :planId
        AND s.plan.user.id = :userId
        """)
    boolean existsByIdAndPlanIdAndUserId(
        @Param("scheduleId") UUID scheduleId,
        @Param("planId") UUID planId,
        @Param("userId") UUID userId
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1 FROM study_plan_schedule s
            JOIN study_plans p ON s.plan_id = p.id
            WHERE p.user_id = :userId
            AND s.start_time < :endTime
            AND (s.start_time + (s.duration_min * interval '1 minute')) > :startTime
            AND (:excludeScheduleId IS NULL OR s.id != :excludeScheduleId)
        )
        """, nativeQuery = true)
    boolean existsOverlap(
        @Param("userId") UUID userId,
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime,
        @Param("excludeScheduleId") UUID excludeScheduleId
    );
}