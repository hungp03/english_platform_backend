package com.english.api.course.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(name = "lessons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"module_id", "position"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    private String title;
    private String kind;
    private Integer estimatedMin;
    private Integer position;
    private Boolean isFree;

    // List of content blocks
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonBlock> blocks = new ArrayList<>();

    // Lesson-level media (PDF, transcript, etc.)
    @ManyToMany
    @JoinTable(
            name = "lesson_assets",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private List<MediaAsset> assets = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
    }
}


