package com.english.api.assessment.model;

import com.english.api.assessment.model.enums.QuizAttemptStatus;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.user.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "quiz_attempts",
        indexes = {
                @Index(name = "idx_quiz_attempts_user", columnList = "user_id"),
                @Index(name = "idx_quiz_attempts_quiz", columnList = "quiz_id")
        }
)
@BatchSize(size = 20)
public class QuizAttempt {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizSkill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizAttemptStatus status;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 30) // gom 30 answer/lần khi load
    private List<QuizAttemptAnswer> answers = new ArrayList<>();

    @Builder.Default
    private int totalQuestions = 0;

    @Builder.Default
    private int totalCorrect = 0;

    @Builder.Default
    private Double score = 0.0;

    @Builder.Default
    private Double maxScore = 0.0;

    @Builder.Default
    private Instant startedAt = Instant.now();

    private Instant submittedAt;

    @Column(name = "completion_time_seconds")
    private Integer completionTimeSeconds;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7 style
        }
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (startedAt == null) startedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // Factory method tiện cho AttemptServiceImpl
    public static QuizAttempt of(Quiz quiz, User user, QuizSkill skill, QuizAttemptStatus status) {
        return QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .skill(skill)
                .status(status)
                .startedAt(Instant.now())
                .build();
    }
}
