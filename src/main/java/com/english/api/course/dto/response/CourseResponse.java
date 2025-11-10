package com.english.api.course.dto.response;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
@Getter
public class CourseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    // Getters
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String detailedDescription;
    private String language;
    private String thumbnail;
    private List<String> skillFocus;
    private Long priceCents;
    private String currency;
    private String status;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructor for JPQL
    public CourseResponse(UUID id, String title, String slug, String description,
                       String detailedDescription, String language, String thumbnail,
                       List<String> skillFocus, Long priceCents, String currency,
                       String status, Instant publishedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.detailedDescription = detailedDescription;
        this.language = language;
        this.thumbnail = thumbnail;
        this.skillFocus = skillFocus;
        this.priceCents = priceCents;
        this.currency = currency;
        this.status = status;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // For compatibility with existing code that expects record-like access
    public UUID id() { return id; }
    public String title() { return title; }
    public String slug() { return slug; }
    public String description() { return description; }
    public String detailedDescription() { return detailedDescription; }
    public String language() { return language; }
    public String thumbnail() { return thumbnail; }
    public List<String> skillFocus() { return skillFocus; }
    public Long priceCents() { return priceCents; }
    public String currency() { return currency; }
    public String status() { return status; }
    public Instant publishedAt() { return publishedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
