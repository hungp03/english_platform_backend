package com.english.api.order.model;

import com.english.api.course.model.Course;
import com.english.api.user.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        }
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }
}
