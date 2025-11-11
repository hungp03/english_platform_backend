package com.english.api.enrollment.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "study_plan_schedule",
    indexes = {
        @Index(name = "idx_schedule_plan", columnList = "plan_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "study_plan_schedule_plan_id_start_time_key", 
                          columnNames = {"plan_id", "start_time"})
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanSchedule {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyPlan plan;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "task_desc", nullable = false, columnDefinition = "text")
    private String taskDesc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "sync_to_calendar", nullable = false)
    @Builder.Default
    private Boolean syncToCalendar = false;

    @Column(name = "google_calendar_event_id")
    private String googleCalendarEventId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum TaskStatus {
        PENDING, COMPLETED
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = TaskStatus.PENDING;
        }
        if (syncToCalendar == null) {
            syncToCalendar = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
