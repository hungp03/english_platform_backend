package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.*;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;

import com.english.api.enrollment.repository.EnrollmentRepository;
import com.english.api.course.dto.request.CreateReviewRequest;
import com.english.api.course.dto.request.UpdateReviewRequest;
import com.english.api.course.dto.response.CourseRatingStatsResponse;
import com.english.api.course.dto.response.MyReviewResponse;
import com.english.api.course.dto.response.ReviewResponse;
import com.english.api.course.dto.response.ReviewSummaryResponse;
import com.english.api.course.mapper.ReviewMapper;
import com.english.api.course.model.CourseReview;
import com.english.api.course.repository.CourseReviewRepository;
import com.english.api.course.service.ReviewService;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    
    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    
    @Override
    @Transactional
    public ReviewResponse createReview(UUID courseId, CreateReviewRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        // 1. Check if course exists
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        // 2. Check if user already reviewed this course
        if (reviewRepository.existsByCourseIdAndUserId(courseId, currentUserId)) {
            throw new ResourceAlreadyExistsException("You have already reviewed this course");
        }
        
        // 3. Check if user is enrolled in the course
        // Enrollment enrollment = enrollmentRepository
        //     .findByUserIdAndCourseId(currentUserId, courseId)
        //     .orElseThrow(() -> new OperationNotAllowedException(
        //         "You must be enrolled in this course to write a review"
        //     ));
        if (!enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
            throw new OperationNotAllowedException("You must be enrolled in this course to write a review");
        }
        
        // 4. Get current user
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // 5. Create review
        CourseReview review = CourseReview.builder()
            .course(course)
            .user(currentUser)
            .rating(request.rating())
            .comment(request.comment())
            .isPublished(true)
            .build();
        
        CourseReview savedReview = reviewRepository.save(review);
        log.info("User {} created review for course {}", currentUserId, courseId);
        
        return reviewMapper.toResponse(savedReview);
    }
    
    @Override
    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        // 1. Find review
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // 2. Check if current user is the author
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only edit your own reviews");
        }
        
        // 3. Update review
        review.setRating(request.rating());
        review.setComment(request.comment());
        
        CourseReview updatedReview = reviewRepository.save(review);
        log.info("User {} updated review {}", currentUserId, reviewId);
        
        return reviewMapper.toResponse(updatedReview);
    }
    
    @Override
    @Transactional
    public void deleteReview(UUID reviewId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        // 1. Find review
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // 2. Check if current user is the author
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own reviews");
        }
        
        // 3. Delete review
        reviewRepository.delete(review);
        log.info("User {} deleted review {}", currentUserId, reviewId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        return reviewMapper.toResponse(review);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getMyReviewForCourse(UUID courseId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        return reviewRepository.findByCourseIdAndUserId(courseId, currentUserId)
            .map(reviewMapper::toResponse)
            .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getReviewsForCourse(UUID courseId, int page, int size) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseReview> reviewsPage = reviewRepository
            .findByCourseIdAndIsPublishedTrue(courseId, pageable);
        
        Page<ReviewSummaryResponse> responsePage = reviewsPage
            .map(reviewMapper::toSummaryResponse);
        
        return PaginationResponse.from(responsePage, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getMyReviews(int page, int size) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseReview> reviewsPage = reviewRepository.findByUserId(currentUserId, pageable);
        
        Page<MyReviewResponse> responsePage = reviewsPage.map(reviewMapper::toMyReviewResponse);
        
        return PaginationResponse.from(responsePage, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseRatingStatsResponse getCourseRatingStats(UUID courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        // Get total reviews
        Long totalReviews = reviewRepository.countByCourseId(courseId);
        
        // Get average rating
        Double averageRating = reviewRepository.calculateAverageRating(courseId);
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // Round to 1 decimal place (e.g., 4.5)
        averageRating = Math.round(averageRating * 10.0) / 10.0;
        
        // Get count by each star rating
        Long fiveStarCount = reviewRepository.countByCourseIdAndRating(courseId, 5);
        Long fourStarCount = reviewRepository.countByCourseIdAndRating(courseId, 4);
        Long threeStarCount = reviewRepository.countByCourseIdAndRating(courseId, 3);
        Long twoStarCount = reviewRepository.countByCourseIdAndRating(courseId, 2);
        Long oneStarCount = reviewRepository.countByCourseIdAndRating(courseId, 1);
        
        return new CourseRatingStatsResponse(
            totalReviews,
            averageRating,
            fiveStarCount,
            fourStarCount,
            threeStarCount,
            twoStarCount,
            oneStarCount
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getReviewsForInstructor(UUID courseId, Boolean isPublished, Integer rating, int page, int size) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
    
        // 1. Kiểm tra khóa học có tồn tại và thuộc về Instructor này không
        UUID ownerId = courseRepository.findOwnerIdById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                
        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("You are not allowed to manage reviews for this course");
        }
    
        // 2. Gọi Repository với bộ lọc
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseReview> reviewsPage = reviewRepository.findByCourseIdWithFilters(courseId, isPublished, rating, pageable);
        
        // 3. Map sang Response (Full details để instructor xem)
        Page<ReviewResponse> responsePage = reviewsPage.map(reviewMapper::toResponse);
    
        return PaginationResponse.from(responsePage, pageable);
    }
    
    @Override
    @Transactional
    public ReviewResponse showReview(UUID reviewId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
    
        // 1. Tìm review
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
            
        // 2. LOGIC MỚI: Kiểm tra quyền sở hữu khóa học
        if (!review.getCourse().getCreatedBy().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only show reviews for your own courses");
        }
    
        // 3. Thực hiện hiện
        review.setIsPublished(true);
        CourseReview updatedReview = reviewRepository.save(review);
        
        log.info("Instructor {} showed review {}", currentUserId, reviewId);
        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    @Transactional
    public ReviewResponse hideReview(UUID reviewId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        if (!review.getCourse().getCreatedBy().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only hide reviews for your own courses");
        }
    
        // 3. Thực hiện ẩn
        review.setIsPublished(false);
        CourseReview updatedReview = reviewRepository.save(review);
        
        log.info("Instructor {} hid review {}", currentUserId, reviewId);
        return reviewMapper.toResponse(updatedReview);
    }

}
