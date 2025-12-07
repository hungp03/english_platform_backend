package com.english.api.enrollment.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.mapper.LessonMapper;
import com.english.api.course.model.Course;
import com.english.api.course.model.Lesson;
import com.english.api.course.repository.CourseModuleRepository;
import com.english.api.course.repository.CourseRepository;
import com.english.api.course.repository.LessonRepository;
import com.english.api.enrollment.dto.projection.EnrollmentProjection;
import com.english.api.enrollment.dto.response.CourseModuleWithLessonsResponse;
import com.english.api.enrollment.dto.response.EnrollmentDetailResponse;
import com.english.api.enrollment.dto.response.EnrollmentResponse;
import com.english.api.enrollment.dto.response.LessonWithProgressResponse;
import com.english.api.enrollment.mapper.EnrollmentMapper;
import com.english.api.enrollment.model.Enrollment;
import com.english.api.enrollment.model.enums.EnrollmentStatus;
import com.english.api.enrollment.repository.EnrollmentRepository;
import com.english.api.enrollment.repository.LessonProgressRepository;
import com.english.api.enrollment.service.EnrollmentService;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.enums.OrderItemEntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of EnrollmentService
 * Created by hungpham on 10/29/2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional
    public void createEnrollmentsAfterPayment(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            log.warn("Cannot create enrollments: Order or items are null/empty");
            return;
        }

        log.info("Creating enrollments for order ID: {}", order.getId());

        // Extract all course IDs from order items (avoiding N+1)
        List<UUID> courseIds = order.getItems().stream()
                .filter(item -> item.getEntity() == OrderItemEntityType.COURSE)
                .map(OrderItem::getEntityId)
                .toList();

        if (courseIds.isEmpty()) {
            log.info("No course items found in order {}", order.getId());
            return;
        }

        try {
            UUID userId = order.getUser().getId();

            // Batch check: Get all already enrolled course IDs in one query
            Set<UUID> alreadyEnrolledCourseIds = enrollmentRepository.findEnrolledCourseIds(userId, courseIds);
            log.debug("User {} already enrolled in {} courses from this order", userId, alreadyEnrolledCourseIds.size());

            // Filter out already enrolled courses
            List<UUID> courseIdsToEnroll = courseIds.stream()
                    .filter(courseId -> !alreadyEnrolledCourseIds.contains(courseId))
                    .toList();

            if (courseIdsToEnroll.isEmpty()) {
                log.info("User {} is already enrolled in all courses from order {}", userId, order.getId());
                return;
            }

            // Batch fetch: Get all courses in one query
            List<Course> courses = courseRepository.findAllById(courseIdsToEnroll);
            
            if (courses.size() != courseIdsToEnroll.size()) {
                log.warn("Some courses not found. Expected: {}, Found: {}", courseIdsToEnroll.size(), courses.size());
            }

            // Create a map for quick lookup
            Map<UUID, Course> courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getId, course -> course));

            // Build all enrollments
            List<Enrollment> enrollmentsToCreate = new ArrayList<>();
            OffsetDateTime now = OffsetDateTime.now();

            for (UUID courseId : courseIdsToEnroll) {
                Course course = courseMap.get(courseId);
                if (course != null) {
                    Enrollment enrollment = Enrollment.builder()
                            .user(order.getUser())
                            .course(course)
                            .status(EnrollmentStatus.ACTIVE)
                            .startedAt(now)
                            .build();
                    enrollmentsToCreate.add(enrollment);
                } else {
                    log.warn("Course not found with ID: {}, skipping enrollment", courseId);
                }
            }

            // Batch insert: Save all enrollments at once
            if (!enrollmentsToCreate.isEmpty()) {
                enrollmentRepository.saveAll(enrollmentsToCreate);
                log.info("Created {} enrollments for user {} in order {}", 
                        enrollmentsToCreate.size(), userId, order.getId());
            }

        } catch (Exception e) {
            log.error("Failed to create enrollments for order {}: {}", 
                    order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create enrollments", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Fetching all enrollments for user: {}", currentUserId);
        
        // Fetch all enrollments with course eagerly loaded
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourse(currentUserId);
        
        // Convert to response DTOs
        List<EnrollmentResponse> responseList = enrollments.stream()
                .map(enrollmentMapper::toEnrollmentResponse)
                .toList();
        
        log.info("Found {} enrollments for user {}", responseList.size(), currentUserId);
        
        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentDetailResponse getEnrollmentDetails(String courseSlug) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Fetching enrollment details for user {} and course slug {}", currentUserId, courseSlug);

        // Use optimized projection query to avoid loading user entity
        EnrollmentProjection enrollmentData = enrollmentRepository.findEnrollmentProjectionByUserIdAndCourseSlug(currentUserId, courseSlug)
                .orElseThrow(() -> {
                    log.warn("User {} attempted to access course {} without enrollment", currentUserId, courseSlug);
                    return new AccessDeniedException("You are not enrolled in this course");
                });
        
        // Get published modules with lesson count
        List<CourseModuleResponse> publishedModules = courseModuleRepository.findPublishedModulesWithLessonCount(enrollmentData.getCourseId());
        
        // Fetch all lessons with progress for the course in one query (optimized)
        List<LessonWithProgressResponse> allLessons = lessonRepository.findPublishedLessonsWithProgressByCourseId(enrollmentData.getCourseId(), currentUserId);
        
        // Group lessons by module ID in memory
        Map<UUID, List<LessonWithProgressResponse>> lessonsByModule = allLessons.stream()
                .collect(Collectors.groupingBy(LessonWithProgressResponse::moduleId));
        
        // Build modules with their lessons
        List<CourseModuleWithLessonsResponse> modulesWithLessons = publishedModules.stream()
                .map(module -> new CourseModuleWithLessonsResponse(
                        module.id(),
                        module.title(),
                        module.position(),
                        module.published(),
                        lessonsByModule.getOrDefault(module.id(), List.of())
                ))
                .toList();

        // Get last completed lesson ID
        UUID lastCompletedLessonId = lessonProgressRepository
                .findLastCompletedLessonIdByUserAndCourse(currentUserId, enrollmentData.getCourseId())
                .orElse(null);
        
        log.info("Found {} published modules for course {} (slug: {}) and user {}", 
                modulesWithLessons.size(), enrollmentData.getCourseId(), courseSlug, currentUserId);

        return new EnrollmentDetailResponse(
                enrollmentData.getEnrollmentId(),
                enrollmentData.getCourseId(),
                enrollmentData.getCourseTitle(),
                enrollmentData.getProgressPercent(),
                modulesWithLessons,
                lastCompletedLessonId
        );
    } 
    
    @Override
    @Transactional(readOnly = true)
    public LessonResponse getLessonWithEnrollmentCheck(UUID lessonId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Fetching lesson {} for user {} with enrollment verification", lessonId, currentUserId);

        boolean isEnrolled = enrollmentRepository.isUserEnrolledInLessonCourse(currentUserId, lessonId);
        if (!isEnrolled) {
            log.warn("User {} attempted to access lesson {} without enrollment", currentUserId, lessonId);
            throw new AccessDeniedException("You are not enrolled in this course or lesson does not exist");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        log.info("User {} accessed lesson {} (title: {})", currentUserId, lessonId, lesson.getTitle());
        return lessonMapper.toResponse(lesson);
    }
}
