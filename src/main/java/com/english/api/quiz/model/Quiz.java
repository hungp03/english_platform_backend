// package com.english.api.quiz.model;

// import jakarta.persistence.*;
// import lombok.*;
// import com.github.f4b6a3.uuid.UuidCreator;

// import java.time.Instant;
// import java.util.LinkedHashSet;
// import java.util.Set;
// import java.util.UUID;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Entity
// @Table(name = "quizzes")
// public class Quiz {

//     @Id
//     private UUID id;

//     @Column(nullable = false, length = 255)
//     private String title;

//     @Column(length = 2000)
//     private String description;

//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false, length = 20)
//     private QuizStatus status;

//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false, length = 20)
//     private QuizSkill skill;

//     @ManyToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(name = "quiz_type_id", nullable = false)
//     private QuizType quizType;

//     @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
//     @OrderBy("orderIndex ASC")
//     private Set<Question> questions = new LinkedHashSet<>();

//     @Column(nullable = false, updatable = false)
//     private Instant createdAt;

//     @Column(nullable = false)
//     private Instant updatedAt;

//     @PrePersist
//     public void prePersist() {
//         if (id == null) {
//             id = UuidCreator.getTimeOrdered();
//         }
//         if (status == null) {
//             status = QuizStatus.DRAFT;
//         }
//         if (skill == null) {
//             skill = QuizSkill.READING;
//         }
//         Instant now = Instant.now();
//         createdAt = now;
//         updatedAt = now;
//     }

//     @PreUpdate
//     public void preUpdate() {
//         updatedAt = Instant.now();
//     }
// }
package com.english.api.quiz.model;

import jakarta.persistence.*;
import java.util.*;
import com.english.api.quiz.enums.QuizSkill;
import com.english.api.quiz.enums.QuizStatus;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quizzes")
// @Entity
// @Table(name = "quiz")
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
    private QuizSkill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status = QuizStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_type_id", nullable = false)
    private QuizType quizType;

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
        if (skill == null) {
            skill = QuizSkill.READING;
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
