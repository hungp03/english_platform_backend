package com.english.api.course.model;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(
    name = "course_modules",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "position"})
    },
    indexes = {
        @Index(name = "idx_course_modules_course_id", columnList = "course_id"),
        @Index(name = "idx_course_modules_published", columnList = "published"),
        @Index(name = "idx_course_modules_course_position", columnList = "course_id, position")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModule {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String title;

    private Integer position;

    @Builder.Default
    private Boolean published = false;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        }
        if (published == null) {
            published = false;
        }
    }
}
