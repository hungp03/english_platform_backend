package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceAlreadyOwnedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.service.MediaService;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseCheckoutResponse;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.dto.response.CourseWithStatsResponse;
import com.english.api.course.dto.response.GrowthPeriodResponse;
import com.english.api.course.dto.response.InstructorStatsResponse;
import com.english.api.course.dto.response.MonthlyGrowthResponse;
import com.english.api.course.mapper.CourseMapper;
import com.english.api.course.model.Course;
import com.english.api.course.model.enums.CourseStatus;
import com.english.api.course.repository.CourseRepository;
import com.english.api.course.service.CourseService;
import com.english.api.order.repository.OrderRepository;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper mapper;
    private final MediaService mediaService;
    private final OrderRepository orderRepository;

    @Override
    public CourseDetailResponse getById(UUID id) {
        return courseRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Override
    @CachePut(value = "courses", key = "#result.id()")
    public CourseDetailResponse getPublishedBySlug(String slug) {
        return courseRepository.findDetailBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional
    @Override
    public CourseResponse create(CourseRequest req) {
        // TEMPORARY: Only allow VND currency for new courses
        // TODO: Remove this restriction when multiple currencies are fully supported
        if (!"VND".equalsIgnoreCase(req.currency())) {
            throw new com.english.api.common.exception.ResourceInvalidException(
                "Currently, only VND currency is supported for course creation");
        }
        
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        Course course = mapper.toEntity(req);
        course.setCreatedBy(User.builder().id(currentUserId).build());
        return mapper.toResponse(courseRepository.save(course));
    }

    @Override
    public PaginationResponse getCourses(Pageable pageable, String keyword, String status, String[] skills) {
        var page = courseRepository.searchWithStats(keyword, status, skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getSlug(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getStatus(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));

        return PaginationResponse.from(page, pageable);
    }

    @Override
    public PaginationResponse getCoursesForInstructor(Pageable pageable, String keyword, String status, String[] skills) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        var page = courseRepository.searchByOwnerWithStats(currentUserId, keyword, status, skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getSlug(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getStatus(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));
        return PaginationResponse.from(page, pageable);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    @Override
    public CourseResponse update(UUID id, CourseRequest req) {
        // TEMPORARY: Only allow VND currency for courses
        // TODO: Remove this restriction when multiple currencies are fully supported
        if (!"VND".equalsIgnoreCase(req.currency())) {
            throw new ResourceInvalidException(
                "Currently, only VND currency is supported for courses");
        }
        
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        UUID ownerId = courseRepository.findOwnerIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("You are not allowed to update this course.");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.setTitle(req.title());
        course.setDescription(req.description());
        course.setDetailedDescription(req.detailedDescription());
        course.setLanguage(req.language());
        // Store the old thumbnail URL before updating
        String oldThumbnail = course.getThumbnail();
        if (req.thumbnail() != null && !req.thumbnail().isBlank()) {
            if (oldThumbnail != null && !oldThumbnail.isBlank() && !oldThumbnail.equals(req.thumbnail())) {
                mediaService.deleteFileByUrl(oldThumbnail);
            }
            course.setThumbnail(req.thumbnail());
        }

        if (req.skillFocus() != null) {
            course.setSkillFocus(req.skillFocus().toArray(new String[0]));
        }

        course.setPriceCents(req.priceCents());
        course.setCurrency(req.currency());

        return mapper.toResponse(courseRepository.save(course));
    }


    @Override
    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public void delete(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        UUID ownerId = courseRepository.findOwnerIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("You are not allowed to delete this course.");
        }

        courseRepository.softDeleteById(id, Instant.now());
    }


    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    @Override
    public CourseResponse changeStatus(UUID id, CourseStatus status) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        UUID ownerId = courseRepository.findOwnerIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean isOwner = ownerId.equals(currentUserId);

        // Check authorization: must be either owner or admin
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to change status of this course.");
        }

        // If admin but NOT owner: can only approve (PUBLISHED) or reject (REJECTED)
        if (isAdmin && !isOwner && status != CourseStatus.PUBLISHED && status != CourseStatus.REJECTED) {
            throw new AccessDeniedException("Admin can only approve or reject courses.");
        }

        // Creator (non-admin) cannot self-reject
        if (isOwner && !isAdmin && status == CourseStatus.REJECTED) {
            throw new AccessDeniedException("You cannot reject your own course.");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.setStatus(status);
        // Set publishedAt when status is PUBLISHED
        if (status == CourseStatus.PUBLISHED) {
            course.setPublishedAt(Instant.now());
        } else {
            course.setPublishedAt(null);
        }
        return mapper.toResponse(courseRepository.save(course));
    }

    private boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public PaginationResponse getPublishedCourses(Pageable pageable, String keyword, String[] skills) {
        var page = courseRepository.searchWithStats(keyword, "PUBLISHED", skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getSlug(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getStatus(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));
        return PaginationResponse.from(page, pageable);
    }


    /**
     * Gets minimal course information needed for checkout payment display.
     * Only returns essential fields to minimize data transfer.
     * Throws exception if user has already purchased the course.
     */
    @Override
    public CourseCheckoutResponse getCheckoutInfoById(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        // Check if user has already purchased this course
        if (orderRepository.hasUserPurchasedCourse(currentUserId, id)) {
            throw new ResourceAlreadyOwnedException("You have already purchased this course");
        }
        
        return courseRepository.findCheckoutInfoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }
    
    @Override
    public InstructorStatsResponse getInstructorStats(UUID instructorId) {
        Object[] result = courseRepository.getInstructorStats(instructorId);
        
        if (result == null || result.length == 0) {
            // Return zero stats if no data found
            return InstructorStatsResponse.builder()
                    .totalCourses(0L)
                    .publishedCourses(0L)
                    .totalStudents(0L)
                    .totalRevenueCents(0L)
                    .formattedRevenue("0đ")
                    .build();
        }
        
        // PostgreSQL function returns a table (single row), so result[0] is the row data
        // Cast the first element to Object[] to access individual columns
        Object[] row = (Object[]) result[0];
        
        // Extract values from the row
        Long totalCourses = row[0] != null ? ((Number) row[0]).longValue() : 0L;
        Long publishedCourses = row[1] != null ? ((Number) row[1]).longValue() : 0L;
        Long totalStudents = row[2] != null ? ((Number) row[2]).longValue() : 0L;
        Long totalRevenueCents = row[3] != null ? ((Number) row[3]).longValue() : 0L;
        
        // Format revenue as Vietnamese currency
        NumberFormat vndFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        String formattedRevenue = vndFormat.format(totalRevenueCents) + "đ";
        
        return InstructorStatsResponse.builder()
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .totalStudents(totalStudents)
                .totalRevenueCents(totalRevenueCents)
                .formattedRevenue(formattedRevenue)
                .build();
    }
    
    @Override
    public MonthlyGrowthResponse getMonthlyGrowth(UUID instructorId, Integer year, Integer month) {
        // Use optimized PostgreSQL function - single query instead of 10 queries
        List<Object[]> results = courseRepository.getMonthlyGrowth(instructorId, year, month);
        
        List<GrowthPeriodResponse> periods = new ArrayList<>();
        long totalRevenueCents = 0L;
        long totalStudents = 0L;
        NumberFormat vndFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        
        // Process results from PostgreSQL function
        for (Object[] row : results) {
            // Extract values: [period_start, period_end, revenue_cents, student_count]
            Integer periodStart = ((Number) row[0]).intValue();
            Integer periodEnd = ((Number) row[1]).intValue();
            Long revenueCents = ((Number) row[2]).longValue();
            Long studentCount = ((Number) row[3]).longValue();
            
            totalRevenueCents += revenueCents;
            totalStudents += studentCount;
            
            LocalDate startDate = LocalDate.of(year, month, periodStart);
            LocalDate endDate = LocalDate.of(year, month, periodEnd);
            String periodLabel = periodStart + "-" + periodEnd;
            String formattedRevenue = vndFormat.format(revenueCents) + "đ";
            
            periods.add(GrowthPeriodResponse.builder()
                .periodLabel(periodLabel)
                .startDate(startDate)
                .endDate(endDate)
                .revenueCents(revenueCents)
                .formattedRevenue(formattedRevenue)
                .studentCount(studentCount)
                .build());
        }
        
        String formattedTotalRevenue = vndFormat.format(totalRevenueCents) + "đ";
        
        return MonthlyGrowthResponse.builder()
                .year(year)
                .month(month)
                .totalRevenueCents(totalRevenueCents)
                .formattedTotalRevenue(formattedTotalRevenue)
                .totalStudents(totalStudents)
                .periods(periods)
                .build();
    }

    @Override
    public PaginationResponse getPublishedByInstructor(UUID instructorId, Pageable pageable, String keyword, String[] skills) {
        var page = courseRepository.searchByOwnerWithStats(instructorId, keyword, "PUBLISHED", skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getSlug(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getStatus(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));
        return PaginationResponse.from(page, pageable);
    }

}
