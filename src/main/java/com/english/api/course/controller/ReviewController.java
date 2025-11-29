package com.english.api.course.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CreateReviewRequest;
import com.english.api.course.dto.request.UpdateReviewRequest;
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

/**
 * REST Controller for Course Reviews
 * Handles student review operations
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * Create a new review for a course
     * POST /api/reviews/courses/{courseId}
     * 
     * @param courseId Course to review
     * @param request Review data (rating + comment)
     * @return Created review
     */
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateReviewRequest request) {
        
        ReviewResponse review = reviewService.createReview(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }
    
    /**
     * Update an existing review
     * PUT /api/reviews/{reviewId}
     * 
     * @param reviewId Review to update
     * @param request Updated data
     * @return Updated review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        
        ReviewResponse review = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Delete a review
     * DELETE /api/reviews/{reviewId}
     * 
     * @param reviewId Review to delete
     * @return Success message
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }
    
    /**
     * Get a specific review by ID
     * GET /api/reviews/{reviewId}
     * 
     * @param reviewId Review ID
     * @return Review details
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Get current user's review for a course
     * GET /api/reviews/my-review/courses/{courseId}
     * 
     * @param courseId Course ID
     * @return User's review or null if not reviewed yet
     */
    @GetMapping("/my-review/courses/{courseId}")
    public ResponseEntity<ReviewResponse> getMyReviewForCourse(@PathVariable UUID courseId) {
        ReviewResponse review = reviewService.getMyReviewForCourse(courseId);
        if (review == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(review);
    }
    
    /**
     * Get all my reviews
     * GET /api/reviews/my-reviews
     * 
     * @param pageable Pagination parameters (default: page 0, size 20)
     * @return Paginated list of user's reviews
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<PaginationResponse> getMyReviews(
            @PageableDefault(size = 20) Pageable pageable) {
        
        PaginationResponse reviews = reviewService.getMyReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/courses/{courseId}")
    public ResponseEntity<PaginationResponse> getReviewsForCourse(
            @PathVariable UUID courseId,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        PaginationResponse response = reviewService.getReviewsForInstructor(courseId, isPublished, rating, pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/instructor/{reviewId}/hide")
    public ResponseEntity<ReviewResponse> hideReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.hideReview(reviewId);
        return ResponseEntity.ok(review);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/instructor/{reviewId}/show")
    public ResponseEntity<ReviewResponse> showReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.showReview(reviewId);
        return ResponseEntity.ok(review);
    }
}
