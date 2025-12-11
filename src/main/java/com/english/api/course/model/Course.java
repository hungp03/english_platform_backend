package com.english.api.course.model;

import com.english.api.common.util.SlugUtil;
import com.english.api.course.model.enums.CourseStatus;
import com.english.api.enrollment.model.Enrollment;
import com.english.api.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "courses",
    indexes = {
        @Index(name = "idx_courses_slug", columnList = "slug"),
        @Index(name = "idx_courses_status", columnList = "status"),
        @Index(name = "idx_courses_created_by", columnList = "created_by"),
        @Index(name = "idx_courses_status_published", columnList = "status, published_at"),
        @Index(name = "idx_courses_created_at", columnList = "created_at"),
        @Index(name = "idx_courses_is_deleted", columnList = "is_deleted")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    private String description;

    @Column(name = "detailed_description", columnDefinition = "text")
    private String detailedDescription;

    private String language;

    private String thumbnail;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "course_skills",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @Column(name = "price_cents")
    private Long priceCents;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

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

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Enrollment> enrollments = new HashSet<>();

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
