package com.english.api.course.dto.projection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Projection for Course with statistics
 */
public interface CourseWithStatsProjection {
    UUID getId();
    String getTitle();
    String getSlug();
    String getDescription();
    String getLanguage();
    String getThumbnail();
    List<String> getSkillFocus();
    Long getPriceCents();
    String getCurrency();
    String getStatus();
    Long getModuleCount();
    Long getLessonCount();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    Double getAverageRating();
    Long getTotalReviews();
    Long getStudentCount();
}
