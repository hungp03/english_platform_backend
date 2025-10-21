package com.english.api.course.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/14/2025
 */
public class CourseWithStatsResponse {
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String language;
    private String thumbnail;
    private String[] skillFocus;
    private Long priceCents;
    private String currency;
    private String status;
    private Long moduleCount;
    private Long lessonCount;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructor for JPQL
    public CourseWithStatsResponse(UUID id, String title, String slug, String description,
                                String language, String thumbnail, String[] skillFocus,
                                Long priceCents, String currency, String status,
                                Long moduleCount, Long lessonCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.language = language;
        this.thumbnail = thumbnail;
        this.skillFocus = skillFocus;
        this.priceCents = priceCents;
        this.currency = currency;
        this.status = status;
        this.moduleCount = moduleCount;
        this.lessonCount = lessonCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getLanguage() { return language; }
    public String getThumbnail() { return thumbnail; }
    public String[] getSkillFocus() { return skillFocus; }
    public Long getPriceCents() { return priceCents; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public Long getModuleCount() { return moduleCount; }
    public Long getLessonCount() { return lessonCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // For compatibility with existing code that expects record-like access
    public UUID id() { return id; }
    public String title() { return title; }
    public String slug() { return slug; }
    public String description() { return description; }
    public String language() { return language; }
    public String thumbnail() { return thumbnail; }
    public String[] skillFocus() { return skillFocus; }
    public Long priceCents() { return priceCents; }
    public String currency() { return currency; }
    public String status() { return status; }
    public Long moduleCount() { return moduleCount; }
    public Long lessonCount() { return lessonCount; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
