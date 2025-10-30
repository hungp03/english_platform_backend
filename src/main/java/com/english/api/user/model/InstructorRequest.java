package com.english.api.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;

/**
 * InstructorRequest entity - Stores instructor application requests
 * Created by hungpham on 10/29/2025
 */
@Entity
@Table(
    name = "instructor_requests",
    indexes = {
        @Index(name = "idx_instructor_requests_user", columnList = "user_id"),
        @Index(name = "idx_instructor_requests_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorRequest {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "expertise", columnDefinition = "TEXT")
    private String expertise;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "qualification", columnDefinition = "TEXT")
    private String qualification;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "requested_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant requestedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", referencedColumnName = "id")
    private User reviewedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
