package com.english.api.review.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.review.dto.response.ReviewResponse;
import com.english.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin REST Controller for Course Reviews
 * Admin moderation operations
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * Get all reviews for a course (including unpublished)
     * GET /api/admin/reviews/courses/{courseId}
     * 
     * @param courseId Course ID
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Paginated list of all reviews
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<PaginationResponse> getAllReviewsForCourse(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PaginationResponse reviews = reviewService.getAllReviewsForCourse(courseId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Hide a review (moderation)
     * POST /api/admin/reviews/{reviewId}/hide
     * 
     * @param reviewId Review to hide
     * @return Updated review
     */
    @PostMapping("/{reviewId}/hide")
    public ResponseEntity<ReviewResponse> hideReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.hideReview(reviewId);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Show a review (restore from hidden)
     * POST /api/admin/reviews/{reviewId}/show
     * 
     * @param reviewId Review to show
     * @return Updated review
     */
    @PostMapping("/{reviewId}/show")
    public ResponseEntity<ReviewResponse> showReview(@PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.showReview(reviewId);
        return ResponseEntity.ok(review);
    }
}
