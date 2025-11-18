package com.english.api.assessment.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "speaking_submissions",
        indexes = {
                @Index(name = "idx_speaking_answer", columnList = "attempt_answer_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeakingSubmission {

    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "attempt_answer_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_speaking_answer")
    )
    private QuizAttemptAnswer attemptAnswer;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(columnDefinition = "text")
    private String transcript;

    // =====================
    // AI Scores (IELTS-like)
    // =====================
    private Double aiFluency;
    private Double aiPronunciation;
    private Double aiGrammar;
    private Double aiVocabulary;

    private Double aiScore;

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
