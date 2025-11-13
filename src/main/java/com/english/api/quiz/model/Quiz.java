package com.english.api.quiz.model;
import org.hibernate.annotations.BatchSize;
import jakarta.persistence.*;
import java.util.*;
import com.english.api.quiz.enums.QuizSkill;
import com.english.api.quiz.enums.QuizStatus;
import com.english.api.quiz.model.QuizSection;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import java.time.Instant;
@BatchSize(size = 20)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quizzes")

public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status = QuizStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_type_id", nullable = false)
    private QuizType quizType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_section_id", nullable = true)
    private QuizSection quizSection;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Question> questions = new ArrayList<>();

    @Column(name = "context_text", columnDefinition = "TEXT")
    private String contextText;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrdered();
        }
        if (status == null) {
            status = QuizStatus.DRAFT;
        }
        // if (skill == null) {
        //     skill = QuizSkill.READING;
        // }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
