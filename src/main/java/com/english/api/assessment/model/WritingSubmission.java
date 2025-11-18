package com.english.api.assessment.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "writing_submissions",
        indexes = {
                @Index(name = "idx_writing_answer", columnList = "attempt_answer_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingSubmission {

    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "attempt_answer_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_writing_answer")
    )
    private QuizAttemptAnswer attemptAnswer;

    // =====================
    // AI Rubrics (IELTS Writing Task 1/2 style)
    // =====================
    private Double aiTaskResponse;
    private Double aiCoherence;
    private Double aiGrammar;
    private Double aiVocabulary;

    private Double aiScore; // Overall score

    @Column(columnDefinition = "text")
    private String feedback;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
