package com.english.api.forum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "forum_categories", uniqueConstraints = {
  @UniqueConstraint(name = "uk_forum_categories_slug", columnNames = "slug")
})
public class ForumCategory {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) private UUID id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String slug;
    private String description;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
