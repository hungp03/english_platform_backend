package com.english.api.content.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_categories",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_content_categories_slug", columnNames = {"slug"})
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ContentCategory {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}