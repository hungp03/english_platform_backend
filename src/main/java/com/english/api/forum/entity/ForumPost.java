package com.english.api.forum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "forum_posts", indexes = {
    @Index(name = "idx_forum_posts_thread", columnList = "thread_id, created_at")
})
public class ForumPost {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    private ForumThread thread;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ForumPost parent;
    @Column(name = "author_id") @JdbcTypeCode(SqlTypes.UUID) private UUID authorId;
    @Column(name = "body_md", nullable = false, columnDefinition = "text") private String bodyMd;
    @Column(name = "is_published", nullable = false) private boolean published = true;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
