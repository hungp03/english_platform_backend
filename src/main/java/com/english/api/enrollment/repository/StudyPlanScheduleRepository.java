package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.StudyPlanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudyPlanScheduleRepository extends JpaRepository<StudyPlanSchedule, UUID> {
}
