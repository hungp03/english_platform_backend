package com.english.api.course.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.response.ReviewResponse;
import com.english.api.course.service.ReviewService;
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
@RequestMapping("/api/instructor/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorReviewController {
    
    private final ReviewService reviewService;
    
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
