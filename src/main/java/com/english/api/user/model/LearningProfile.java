package com.english.api.user.model;

import com.english.api.user.model.enums.EnglishLevel;
import com.english.api.user.model.enums.LearningGoal;
import com.english.api.user.model.enums.PreferredStudyTime;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "learning_profiles")
public class LearningProfile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnglishLevel currentLevel = EnglishLevel.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LearningGoal learningGoal = LearningGoal.GENERAL;

    private Integer targetScore; // IELTS: 0-9, TOEIC: 0-990

    @Builder.Default
    private Integer dailyStudyMinutes = 30; // phút/ngày có thể học

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PreferredStudyTime preferredStudyTime = PreferredStudyTime.EVENING;

    @Builder.Default
    private Integer studyDaysPerWeek = 5;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
