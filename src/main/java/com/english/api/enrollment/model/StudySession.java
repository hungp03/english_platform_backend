package com.english.api.enrollment.model;

import com.english.api.course.model.Course;
import com.english.api.course.model.Lesson;
import com.english.api.user.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "study_sessions",
    indexes = {
        @Index(name = "idx_study_sessions_user", columnList = "user_id"),
        @Index(name = "idx_study_sessions_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_study_sessions_course", columnList = "course_id"),
        @Index(name = "idx_study_sessions_lesson", columnList = "lesson_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySession {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Column(name = "duration_min", insertable = false, updatable = false)
    private Integer durationMin;

    @Column(name = "device_type", columnDefinition = "text")
    private String deviceType;

    @Column(columnDefinition = "text")
    private String source;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (startedAt == null) {
            startedAt = OffsetDateTime.now();
        }
    }
}
