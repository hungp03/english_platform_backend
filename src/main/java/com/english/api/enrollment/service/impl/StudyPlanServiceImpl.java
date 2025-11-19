package com.english.api.enrollment.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.enrollment.dto.request.AIStudyPlanRequest;
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
import com.english.api.enrollment.service.GoogleCalendarService;
import com.english.api.enrollment.service.StudyPlanService;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final GoogleCalendarService googleCalendarService;
    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.ai-plan-url}")
    private String n8nAiPlanUrl;

    @Value("${n8n.webhook.api-key}")
    private String n8nApiKey;

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

        // Sync schedules with Google Calendar
        syncSchedulesToGoogleCalendar(saved.getSchedules(), currentUserId);

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
                    if (scheduleReq.syncToCalendar() != null) {
                        existingSchedule.setSyncToCalendar(scheduleReq.syncToCalendar());
                    }
                } else {
                    StudyPlanSchedule newSchedule = studyPlanMapper.toScheduleEntity(scheduleReq, studyPlan);
                    studyPlan.getSchedules().add(newSchedule);
                }
            }
        }

        StudyPlan updated = studyPlanRepository.save(studyPlan);
        log.info("Updated study plan: {} for user: {}", id, currentUserId);

        // Sync all schedules with Google Calendar
        syncSchedulesToGoogleCalendar(updated.getSchedules(), currentUserId);

        return studyPlanMapper.toDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStudyPlan(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Deleting study plan: {} for user: {}", id, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        // Delete all associated Google Calendar events
        for (StudyPlanSchedule schedule : studyPlan.getSchedules()) {
            if (schedule.getGoogleCalendarEventId() != null) {
                googleCalendarService.deleteCalendarEvent(schedule.getGoogleCalendarEventId(), currentUserId);
            }
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

        // Sync to Google Calendar
        syncScheduleToGoogleCalendar(saved, currentUserId);

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
        if (request.syncToCalendar() != null) {
            schedule.setSyncToCalendar(request.syncToCalendar());
        }

        studyPlanScheduleRepository.save(schedule);
        log.info("Updated schedule {} in plan {} for user {}", scheduleId, planId, currentUserId);

        // Sync to Google Calendar
        syncScheduleToGoogleCalendar(schedule, currentUserId);

        return studyPlanMapper.toDetailResponse(studyPlan);
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID planId, UUID scheduleId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Deleting schedule: {} from plan: {} for user: {}", scheduleId, planId, currentUserId);

        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserIdWithSchedules(planId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found or you don't have permission"));

        StudyPlanSchedule scheduleToDelete = studyPlan.getSchedules().stream()
                .filter(s -> s.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found in this plan"));

        // Delete from Google Calendar if exists
        if (scheduleToDelete.getGoogleCalendarEventId() != null) {
            googleCalendarService.deleteCalendarEvent(scheduleToDelete.getGoogleCalendarEventId(), currentUserId);
        }

        studyPlan.getSchedules().remove(scheduleToDelete);
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

    @Override
    public Object generateAIPlan(AIStudyPlanRequest request) {
        log.debug("Calling n8n AI plan generation with goal: {}", request.goal());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + n8nApiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<AIStudyPlanRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    n8nAiPlanUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
            );

            log.info("Successfully generated AI plan for goal: {}", request.goal());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling n8n webhook for AI plan generation", e);
            throw new RuntimeException("Failed to generate AI plan: " + e.getMessage(), e);
        }
    }

    private void syncSchedulesToGoogleCalendar(List<StudyPlanSchedule> schedules, UUID userId) {
        if (!googleCalendarService.isCalendarIntegrationEnabled(userId)) {
            log.debug("Google Calendar integration not enabled for user {}", userId);
            return;
        }

        for (StudyPlanSchedule schedule : schedules) {
            syncScheduleToGoogleCalendar(schedule, userId);
        }
    }

    private void syncScheduleToGoogleCalendar(StudyPlanSchedule schedule, UUID userId) {
        if (!googleCalendarService.isCalendarIntegrationEnabled(userId)) {
            log.debug("Google Calendar integration not enabled for user {}", userId);
            return;
        }

        try {
            // Check if user wants to sync this schedule to calendar
            if (Boolean.TRUE.equals(schedule.getSyncToCalendar())) {
                if (schedule.getGoogleCalendarEventId() == null) {
                    // Create new event
                    String eventId = googleCalendarService.createCalendarEvent(schedule, userId);
                    if (eventId != null) {
                        schedule.setGoogleCalendarEventId(eventId);
                        studyPlanScheduleRepository.save(schedule);
                    }
                } else {
                    // Update existing event
                    googleCalendarService.updateCalendarEvent(schedule, userId);
                }
            } else {
                // User doesn't want to sync, delete calendar event if it exists
                if (schedule.getGoogleCalendarEventId() != null) {
                    googleCalendarService.deleteCalendarEvent(schedule.getGoogleCalendarEventId(), userId);
                    schedule.setGoogleCalendarEventId(null);
                    studyPlanScheduleRepository.save(schedule);
                }
            }
        } catch (Exception e) {
            log.error("Error syncing schedule {} to Google Calendar", schedule.getId(), e);
        }
    }
}
