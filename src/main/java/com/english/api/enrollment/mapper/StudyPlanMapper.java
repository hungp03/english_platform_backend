package com.english.api.enrollment.mapper;

import com.english.api.enrollment.dto.request.CreateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.request.StudyPlanScheduleRequest;
import com.english.api.enrollment.dto.response.StudyPlanDetailResponse;
import com.english.api.enrollment.dto.response.StudyPlanResponse;
import com.english.api.enrollment.dto.response.StudyPlanScheduleResponse;
import com.english.api.enrollment.model.StudyPlan;
import com.english.api.enrollment.model.StudyPlanSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudyPlanMapper {
    
    StudyPlanResponse toResponse(StudyPlan studyPlan);
    
    StudyPlanDetailResponse toDetailResponse(StudyPlan studyPlan);
    
    StudyPlanScheduleResponse toScheduleResponse(StudyPlanSchedule schedule);
    
    List<StudyPlanScheduleResponse> toScheduleResponseList(List<StudyPlanSchedule> schedules);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "plan", source = "plan")
    @Mapping(target = "status", expression = "java(request.status() != null ? request.status() : com.english.api.enrollment.model.StudyPlanSchedule.TaskStatus.PENDING)")
    StudyPlanSchedule toScheduleEntity(StudyPlanScheduleRequest request, StudyPlan plan);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "plan", source = "plan")
    @Mapping(target = "status", expression = "java(request.status() != null ? request.status() : com.english.api.enrollment.model.StudyPlanSchedule.TaskStatus.PENDING)")
    StudyPlanSchedule toScheduleEntity(CreateStudyPlanScheduleRequest request, StudyPlan plan);
}
