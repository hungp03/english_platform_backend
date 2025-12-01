package com.english.api.course.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CreateReviewRequest;
import com.english.api.course.dto.request.UpdateReviewRequest;
import com.english.api.course.dto.response.CourseRatingStatsResponse;
import com.english.api.course.dto.response.ReviewResponse;
import com.english.api.course.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseReviewController {
    
    private final ReviewService reviewService;
    
    // ==================== Public Endpoints ====================
    
    // Get published reviews for a course
    @GetMapping("/{courseId}/reviews")
    public ResponseEntity<PaginationResponse> getPublicCourseReviews(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse reviews = reviewService.getReviewsForCourse(courseId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    // Get rating statistics for a course
    @GetMapping("/{courseId}/reviews/stats")
    public ResponseEntity<CourseRatingStatsResponse> getCourseRatingStats(@PathVariable UUID courseId) {
        CourseRatingStatsResponse stats = reviewService.getCourseRatingStats(courseId);
        return ResponseEntity.ok(stats);
    }
    
    // ==================== Authenticated User Endpoints ====================
    
    // Create a review for a course
    @PostMapping("/{courseId}/reviews")
    public ResponseEntity<ReviewResponse> createCourseReview(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.createReview(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }
    
    // Get current user's review for a specific course
    @GetMapping("/{courseId}/reviews/me")
    public ResponseEntity<ReviewResponse> getMyReviewForCourse(@PathVariable UUID courseId) {
        ReviewResponse review = reviewService.getMyReviewForCourse(courseId);
        if (review == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(review);
    }
    
    // Get all reviews by current user
    @GetMapping("/reviews/me")
    public ResponseEntity<PaginationResponse> getMyReviews(
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse reviews = reviewService.getMyReviews(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    // Get a specific review by ID
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }
    
    // Update a review
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse review = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(review);
    }
    
    // Delete a review
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }
    
    // ==================== Instructor Endpoints ====================
    
    // Get reviews for instructor's course with filters
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{courseId}/reviews/instructor")
    public ResponseEntity<PaginationResponse> getInstructorCourseReviews(
            @PathVariable UUID courseId,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        PaginationResponse response = reviewService.getReviewsForInstructor(courseId, isPublished, rating, pageable);
        return ResponseEntity.ok(response);
    }
    
    // Hide a review
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/reviews/{reviewId}/hide")
    public ResponseEntity<ReviewResponse> hideReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.hideReview(reviewId);
        return ResponseEntity.ok(review);
    }
    
    // Show a review
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/reviews/{reviewId}/show")
    public ResponseEntity<ReviewResponse> showReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.showReview(reviewId);
        return ResponseEntity.ok(review);
    }
}
