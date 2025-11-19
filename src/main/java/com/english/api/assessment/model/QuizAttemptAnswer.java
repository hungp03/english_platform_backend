package com.english.api.assessment.model;

import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempt_answers", indexes = {@Index(name = "idx_attempt_answers_attempt", columnList = "attempt_id"), @Index(name = "idx_attempt_answers_question", columnList = "question_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptAnswer {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    // @Column(name = "attempt_id", nullable = false)
    // private UUID attemptId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // @Column(name = "selected_option_id")
    // private UUID selectedOptionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", foreignKey = @ForeignKey(name = "fk_attempt_answer_option"))
    private QuestionOption selectedOption;

    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;

    @Column(name = "time_spent_ms")
    private Integer timeSpentMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "attemptAnswer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private SpeakingSubmission speakingSubmission;

    @OneToOne(mappedBy = "attemptAnswer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private WritingSubmission writingSubmission;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    // Factory method tiện tạo mới
    public static QuizAttemptAnswer of(QuizAttempt attempt, Question question) {
        return QuizAttemptAnswer.builder().attempt(attempt).question(question).build();
    }
}
