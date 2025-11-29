package com.english.api.forum.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "forum_threads", indexes = {
        @Index(name = "idx_forum_threads_lastpost", columnList = "last_post_at DESC")
})
public class ForumThread {
    @Id
    private UUID id;

    @Column(name = "author_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID authorId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "body_md", nullable = false, columnDefinition = "text")
    private String bodyMd;

    @Builder.Default
    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Builder.Default
    @Column(name = "reply_count", nullable = false)
    private long replyCount = 0;

    @Column(name = "last_post_at")
    private Instant lastPostAt;

    @Column(name = "last_post_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID lastPostId;

    @Column(name = "last_post_author")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID lastPostAuthor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
