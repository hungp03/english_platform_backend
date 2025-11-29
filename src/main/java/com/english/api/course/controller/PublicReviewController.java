package com.english.api.course.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.response.CourseRatingStatsResponse;
import com.english.api.course.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public REST Controller for Course Reviews
 * No authentication required - anyone can view published reviews
 */
@RestController
@RequestMapping("/api/public/reviews")
@RequiredArgsConstructor
public class PublicReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * Get published reviews for a course (public access)
     * GET /api/public/reviews/courses/{courseId}
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<PaginationResponse> getReviewsForCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        PaginationResponse reviews = reviewService.getReviewsForCourse(courseId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get rating statistics for a course (public access)
     * GET /api/public/reviews/courses/{courseId}/stats
     * 
     * Example response:
     * {
     *   "totalReviews": 200,
     *   "averageRating": 4.5,
     *   "fiveStarCount": 120,
     *   "fourStarCount": 50,
     *   "threeStarCount": 20,
     *   "twoStarCount": 7,
     *   "oneStarCount": 3
     * }
     * 
     * @param courseId Course ID
     * @return Rating statistics
     */
    @GetMapping("/courses/{courseId}/stats")
    public ResponseEntity<CourseRatingStatsResponse> getCourseRatingStats(@PathVariable UUID courseId) {
        CourseRatingStatsResponse stats = reviewService.getCourseRatingStats(courseId);
        return ResponseEntity.ok(stats);
    }
}
