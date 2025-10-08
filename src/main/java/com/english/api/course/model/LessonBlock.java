package com.english.api.course.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(name = "lesson_blocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lesson_id", "position"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonBlock {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    private String blockType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    private Integer position;

    // Optional media for this block (video, image, audio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private MediaAsset media;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UuidCreator.getTimeOrderedEpoch();
    }
}


