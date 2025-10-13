package com.english.api.course.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(name = "lessons",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"module_id", "position"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    private String title;
    private String kind;
    private Integer estimatedMin;
    private Integer position;
    private Boolean isFree = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode content;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonMedia> mediaLinks = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UuidCreator.getTimeOrderedEpoch();
    }

    public Optional<MediaAsset> getPrimaryMedia() {
        return mediaLinks.stream()
                .filter(lm -> lm.getRole() == LessonMediaRole.PRIMARY)
                .map(LessonMedia::getMedia)
                .findFirst();
    }
}


