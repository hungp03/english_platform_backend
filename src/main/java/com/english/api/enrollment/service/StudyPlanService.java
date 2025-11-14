package com.english.api.enrollment.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.enrollment.dto.request.AIStudyPlanRequest;
import com.english.api.enrollment.dto.request.CreateStudyPlanRequest;
import com.english.api.enrollment.dto.request.CreateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.request.UpdateStudyPlanRequest;
import com.english.api.enrollment.dto.request.UpdateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.response.StudyPlanDetailResponse;
import com.english.api.enrollment.dto.response.StudyPlanResponse;
import com.english.api.enrollment.dto.response.StudyPlanScheduleResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudyPlanService {
    StudyPlanResponse createStudyPlan(CreateStudyPlanRequest request);
    StudyPlanDetailResponse updateStudyPlan(UUID id, UpdateStudyPlanRequest request);
    void deleteStudyPlan(UUID id);
    StudyPlanDetailResponse getStudyPlanById(UUID id);
    PaginationResponse getMyStudyPlans(Pageable pageable);
    
    StudyPlanScheduleResponse addSchedule(UUID planId, CreateStudyPlanScheduleRequest request);
    StudyPlanDetailResponse updateSchedule(UUID planId, UUID scheduleId, UpdateStudyPlanScheduleRequest request);
    void deleteSchedule(UUID planId, UUID scheduleId);
    void markScheduleComplete(UUID planId, UUID scheduleId);
    
    Object generateAIPlan(AIStudyPlanRequest request);
}
