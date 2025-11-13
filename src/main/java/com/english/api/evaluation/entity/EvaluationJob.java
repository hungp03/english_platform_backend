package com.english.api.evaluation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evaluation_jobs", indexes = {
        @Index(name = "idx_eval_event_id", columnList = "event_id"),
        @Index(name = "idx_eval_attempt", columnList = "attempt_id")
})
@Getter
@Setter
public class EvaluationJob {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "event_id", length = 255, unique = true)
    private String eventId;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "attempt_id", columnDefinition = "uuid")
    private UUID attemptId;

    @Column(name = "quiz_id", columnDefinition = "uuid")
    private UUID quizId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EvaluationStatus status = EvaluationStatus.PENDING;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "signature_ok")
    private boolean signatureOk;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }
}
