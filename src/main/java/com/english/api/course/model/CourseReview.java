package com.english.api.course.model;

import com.english.api.course.model.Course;
import com.english.api.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * CourseReview Entity
 * Represents a review given by a student for a course they are enrolled in.
 * 
 * Business Rules:
 * - User must be enrolled in the course to write a review
 * - One user can only have one review per course
 * - Rating must be between 1-5 stars
 */
@Entity
@Table(
    name = "course_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_course_reviews_user_course", 
            columnNames = {"user_id", "course_id"}
        )
    },
    indexes = {
        @Index(name = "idx_course_reviews_course", columnList = "course_id"),
        @Index(name = "idx_course_reviews_user", columnList = "user_id"),
        @Index(name = "idx_course_reviews_rating", columnList = "rating"),
        @Index(name = "idx_course_reviews_created", columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReview {
    
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;
    
    /**
     * The course being reviewed
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "course_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_course_reviews_course")
    )
    private Course course;
    
    /**
     * The user who wrote the review (must be enrolled in the course)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_course_reviews_user")
    )
    private User user;
    
    /**
     * Star rating: 1-5 stars
     */
    @Column(nullable = false)
    private Integer rating;
    
    /**
     * Review comment (optional but encouraged)
     */
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    /**
     * Whether this review is visible to public
     * Admin can hide inappropriate reviews
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublished = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * Validation: Rating must be between 1-5
     */
    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }
    }
}
