package com.english.api.enrollment.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;
import com.english.api.enrollment.dto.response.EnrollmentResponse;
import com.english.api.enrollment.mapper.EnrollmentMapper;
import com.english.api.enrollment.model.Enrollment;
import com.english.api.enrollment.model.enums.EnrollmentStatus;
import com.english.api.enrollment.repository.EnrollmentRepository;
import com.english.api.enrollment.service.EnrollmentService;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.enums.OrderItemEntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final EnrollmentMapper enrollmentMapper;

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
    public PaginationResponse getMyEnrollments(Pageable pageable) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("Fetching enrollments for user: {} with pagination", currentUserId);
        
        // Fetch enrollments with course eagerly loaded in one query with pagination
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByUserIdWithCourse(currentUserId, pageable);
        
        // Convert to response DTOs
        Page<EnrollmentResponse> responsePage = enrollmentPage.map(enrollmentMapper::toEnrollmentResponse);
        
        log.info("Found {} enrollments for user {} (page {}/{})", 
                enrollmentPage.getNumberOfElements(), currentUserId, 
                pageable.getPageNumber() + 1, enrollmentPage.getTotalPages());
        
        return PaginationResponse.from(responsePage, pageable);
    }
}
