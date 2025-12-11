package com.english.api.course.model;

import com.english.api.course.model.enums.LessonMediaRole;
import com.english.api.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(
    name = "media_assets",
    indexes = {
        @Index(name = "idx_media_assets_owner_id", columnList = "owner_id"),
        @Index(name = "idx_media_assets_lesson_id", columnList = "lesson_id"),
        @Index(name = "idx_media_assets_mime_type", columnList = "mimeType"),
        @Index(name = "idx_media_assets_created_at", columnList = "createdAt")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LessonMediaRole role = LessonMediaRole.ATTACHMENT;

    private Integer position;

    private String title;

    private String mimeType;

    private String url;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode meta;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        createdAt = Instant.now();
    }
}
