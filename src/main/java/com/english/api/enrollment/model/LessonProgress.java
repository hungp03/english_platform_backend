package com.english.api.enrollment.model;

import com.english.api.course.model.Lesson;
import com.english.api.user.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "lesson_progress",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
    },
    indexes = {
        @Index(name = "idx_lesson_progress_user", columnList = "user_id"),
        @Index(name = "idx_lesson_progress_lesson", columnList = "lesson_id"),
        @Index(name = "idx_lesson_progress_enrollment", columnList = "enrollment_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UuidCreator.getTimeOrderedEpoch();
        if (lastSeenAt == null) lastSeenAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastSeenAt = OffsetDateTime.now();
    }

    // tiện ích nhỏ
    public void markCompleted() {
        this.completed = true;
        this.lastSeenAt = OffsetDateTime.now();
    }
}

