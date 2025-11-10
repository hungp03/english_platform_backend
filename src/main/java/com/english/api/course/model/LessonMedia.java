package com.english.api.course.model;

import com.english.api.course.model.enums.LessonMediaRole;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by hungpham on 10/10/2025
 */
@Entity
@Table(
    name = "lesson_media",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lesson_id", "media_id"})
    },
    indexes = {
        @Index(name = "idx_lesson_media_lesson_id", columnList = "lesson_id"),
        @Index(name = "idx_lesson_media_media_id", columnList = "media_id"),
        @Index(name = "idx_lesson_media_role", columnList = "role"),
        @Index(name = "idx_lesson_media_lesson_role", columnList = "lesson_id, role")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonMedia implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaAsset media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonMediaRole role = LessonMediaRole.ATTACHMENT;

    private Integer position;

    @PrePersist
    public void prePersist() {
        if (id == null) id = com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch();
    }
}

