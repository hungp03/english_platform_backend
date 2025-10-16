package com.english.api.course.model;

import com.english.api.common.util.SlugUtil;
import com.english.api.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Course {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    private String description;

    private String language;

    private String thumbnail;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skill_focus", columnDefinition = "text[]")
    private String[] skillFocus;

    @Column(name = "price_cents")
    private Long priceCents;

    private String currency;

    @Column(name = "is_published")
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = Boolean.FALSE;

    private Instant deletedAt;

    // === Hooks ===
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        }
        if (slug == null || slug.isEmpty()) {
            slug = SlugUtil.toSlugWithUuid(title);
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        slug = SlugUtil.toSlugWithUuid(title);
        updatedAt = Instant.now();
    }
}
