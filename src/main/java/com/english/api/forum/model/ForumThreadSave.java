package com.english.api.forum.model;

import com.english.api.user.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "forum_thread_saves",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_thread_saves_user_thread", columnNames = {"user_id", "thread_id"})
    },
    indexes = {
        @Index(name = "idx_thread_saves_user", columnList = "user_id"),
        @Index(name = "idx_thread_saves_created_at", columnList = "created_at")
    }
)
public class ForumThreadSave {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private ForumThread thread;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}