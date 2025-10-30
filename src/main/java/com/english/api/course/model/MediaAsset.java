package com.english.api.course.model;

import com.english.api.user.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(
    name = "media_assets",
    indexes = {
        @Index(name = "idx_media_assets_owner_id", columnList = "owner_id"),
        @Index(name = "idx_media_assets_mime_type", columnList = "mimeType"),
        @Index(name = "idx_media_assets_created_at", columnList = "createdAt")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String mimeType;

    private String url;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode meta;

    private Instant createdAt;

    @OneToMany(mappedBy = "media", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<LessonMedia> lessonMediaLinks = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        }
        createdAt = Instant.now();
    }
}

