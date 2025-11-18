package com.english.api.quiz.model;

import jakarta.persistence.*;
import lombok.*;
import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quiz_types")
public class QuizType {

    @Id
    private UUID id;


    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(length = 512)
    private String description;

    @OneToMany(mappedBy = "quizType", fetch = FetchType.LAZY)
    private Set<Quiz> quizzes = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrdered();
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