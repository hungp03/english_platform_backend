package com.english.api.enrollment.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.enrollment.dto.request.CreateStudyPlanRequest;
import com.english.api.enrollment.dto.request.CreateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.request.StudyPlanScheduleRequest;
import com.english.api.enrollment.dto.request.UpdateStudyPlanRequest;
import com.english.api.enrollment.dto.request.UpdateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.response.StudyPlanDetailResponse;
import com.english.api.enrollment.dto.response.StudyPlanResponse;
import com.english.api.enrollment.dto.response.StudyPlanScheduleResponse;
import com.english.api.enrollment.mapper.StudyPlanMapper;
import com.english.api.enrollment.model.StudyPlan;
import com.english.api.enrollment.model.StudyPlanSchedule;
import com.english.api.enrollment.repository.StudyPlanRepository;
import com.english.api.enrollment.repository.StudyPlanScheduleRepository;
import com.english.api.enrollment.service.StudyPlanService;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyPlanServiceImpl implements StudyPlanService {
    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanScheduleRepository studyPlanScheduleRepository;
    private final StudyPlanMapper studyPlanMapper;

    @Override
    @Transactional
    public StudyPlanResponse createStudyPlan(CreateStudyPlanRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Creating study plan for user: {}", currentUserId);

        StudyPlan studyPlan = StudyPlan.builder()
                .user(User.builder().id(currentUserId).build())
                .title(request.title())
                .schedules(new ArrayList<>())
                .build();

        if (request.schedules() != null && !request.schedules().isEmpty()) {
            List<StudyPlanSchedule> schedules = request.schedules().stream()
                    .map(scheduleReq -> studyPlanMapper.toScheduleEntity(scheduleReq, studyPlan))
                    .collect(Collectors.toList());
            studyPlan.getSchedules().addAll(schedules);
        }

        StudyPlan saved = studyPlanRepository.save(studyPlan);
        log.info("Created study plan with id: {} for user: {}", saved.getId(), currentUserId);

        return studyPlanMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StudyPlanDetailResponse updateStudyPlan(UUID id, UpdateStudyPlanRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Updating study plan: {} for user: {}", id, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        studyPlan.setTitle(request.title());

        if (request.schedules() == null || request.schedules().isEmpty()) {
            studyPlan.getSchedules().clear();
        } else {
            List<UUID> requestScheduleIds = request.schedules().stream()
                    .map(StudyPlanScheduleRequest::id)
                    .filter(scheduleId -> scheduleId != null)
                    .collect(Collectors.toList());

            studyPlan.getSchedules().removeIf(schedule -> !requestScheduleIds.contains(schedule.getId()));

            for (StudyPlanScheduleRequest scheduleReq : request.schedules()) {
                if (scheduleReq.id() != null) {
                    StudyPlanSchedule existingSchedule = studyPlan.getSchedules().stream()
                            .filter(s -> s.getId().equals(scheduleReq.id()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + scheduleReq.id()));
                    
                    existingSchedule.setStartTime(scheduleReq.startTime());
                    existingSchedule.setDurationMin(scheduleReq.durationMin());
                    existingSchedule.setTaskDesc(scheduleReq.taskDesc());
                    if (scheduleReq.status() != null) {
                        existingSchedule.setStatus(scheduleReq.status());
                    }
                } else {
                    StudyPlanSchedule newSchedule = studyPlanMapper.toScheduleEntity(scheduleReq, studyPlan);
                    studyPlan.getSchedules().add(newSchedule);
                }
            }
        }

        StudyPlan updated = studyPlanRepository.save(studyPlan);
        log.info("Updated study plan: {} for user: {}", id, currentUserId);

        return studyPlanMapper.toDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStudyPlan(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Deleting study plan: {} for user: {}", id, currentUserId);

        if (!studyPlanRepository.existsByIdAndUserId(id, currentUserId)) {
            throw new ResourceNotFoundException("Study plan not found or you don't have permission");
        }

        studyPlanRepository.deleteById(id);
        log.info("Deleted study plan: {} for user: {}", id, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyPlanDetailResponse getStudyPlanById(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Fetching study plan: {} for user: {}", id, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        return studyPlanMapper.toDetailResponse(studyPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getMyStudyPlans(Pageable pageable) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Fetching study plans with schedules for user: {} with pagination", currentUserId);

        Page<StudyPlan> studyPlanPage = studyPlanRepository.findByUserIdWithSchedulesOrderByCreatedAtDesc(currentUserId, pageable);
        Page<StudyPlanDetailResponse> responsePage = studyPlanPage.map(studyPlanMapper::toDetailResponse);

        log.info("Found {} study plans for user {} (page {}/{})",
                studyPlanPage.getNumberOfElements(), currentUserId,
                pageable.getPageNumber() + 1, studyPlanPage.getTotalPages());

        return PaginationResponse.from(responsePage, pageable);
    }

    @Override
    @Transactional
    public StudyPlanScheduleResponse addSchedule(UUID planId, CreateStudyPlanScheduleRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Adding schedule to plan: {} for user: {}", planId, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(planId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        StudyPlanSchedule schedule = studyPlanMapper.toScheduleEntity(request, studyPlan);
        studyPlan.getSchedules().add(schedule);
        
        StudyPlanSchedule saved = studyPlanScheduleRepository.save(schedule);
        log.info("Added schedule {} to plan {} for user {}", saved.getId(), planId, currentUserId);

        return studyPlanMapper.toScheduleResponse(saved);
    }

    @Override
    @Transactional
    public StudyPlanDetailResponse updateSchedule(UUID planId, UUID scheduleId, UpdateStudyPlanScheduleRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Updating schedule: {} in plan: {} for user: {}", scheduleId, planId, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(planId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        StudyPlanSchedule schedule = studyPlan.getSchedules().stream()
                .filter(s -> s.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found in this plan"));

        schedule.setStartTime(request.startTime());
        schedule.setDurationMin(request.durationMin());
        schedule.setTaskDesc(request.taskDesc());
        if (request.status() != null) {
            schedule.setStatus(request.status());
        }

        studyPlanScheduleRepository.save(schedule);
        log.info("Updated schedule {} in plan {} for user {}", scheduleId, planId, currentUserId);

        return studyPlanMapper.toDetailResponse(studyPlan);
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID planId, UUID scheduleId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Deleting schedule: {} from plan: {} for user: {}", scheduleId, planId, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(planId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        boolean removed = studyPlan.getSchedules().removeIf(s -> s.getId().equals(scheduleId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Schedule not found in this plan");
        }

        studyPlanScheduleRepository.deleteById(scheduleId);
        log.info("Deleted schedule {} from plan {} for user {}", scheduleId, planId, currentUserId);
    }

    @Override
    @Transactional
    public void markScheduleComplete(UUID planId, UUID scheduleId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Toggling schedule status: {} in plan: {} for user: {}", scheduleId, planId, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(planId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        StudyPlanSchedule schedule = studyPlan.getSchedules().stream()
                .filter(s -> s.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found in this plan"));

        StudyPlanSchedule.TaskStatus newStatus = schedule.getStatus() == StudyPlanSchedule.TaskStatus.COMPLETED
                ? StudyPlanSchedule.TaskStatus.PENDING
                : StudyPlanSchedule.TaskStatus.COMPLETED;

        schedule.setStatus(newStatus);

        studyPlanScheduleRepository.save(schedule);
        log.info("Toggled schedule {} status to {} in plan {} for user {}",
                scheduleId, newStatus, planId, currentUserId);
    }
}
