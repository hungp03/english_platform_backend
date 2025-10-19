package com.english.api.course.dto.projection;

import java.time.Instant;
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
    String[] getSkillFocus();
    Long getPriceCents();
    String getCurrency();
    Boolean getIsPublished();
    Long getModuleCount();
    Long getLessonCount();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
