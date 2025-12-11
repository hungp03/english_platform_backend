package com.english.api.course.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "skills",
    indexes = {
        @Index(name = "idx_skills_name", columnList = "name", unique = true)
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "skills")
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        createdAt = Instant.now();
    }
}
