package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.StudyPlanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}