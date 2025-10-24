// package com.english.api.content.model;

// import com.english.api.user.model.User;
// import jakarta.persistence.*;
// import lombok.*;
// import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.UpdateTimestamp;

// import java.time.Instant;
// import java.util.UUID;

// @Entity
// @Table(name = "content_comments")
// @Getter @Setter
// @NoArgsConstructor @AllArgsConstructor @Builder
// public class ContentComment {

//     @Id
//     @GeneratedValue
//     @Column(columnDefinition = "uuid")
//     private UUID id;

//     @ManyToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(name = "post_id", nullable = false)
//     private ContentPost post;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "parent_id")
//     private ContentComment parent; // null = root comment

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "author_id")
//     private User author; // nullable -> ON DELETE SET NULL

//     @Column(name = "body_md", columnDefinition = "text", nullable = false)
//     private String bodyMd;

//     @Column(name = "is_published", nullable = false)
//     private boolean published;

//     @CreationTimestamp
//     @Column(name = "created_at", nullable = false, updatable = false)
//     private Instant createdAt;

//     @UpdateTimestamp
//     @Column(name = "updated_at", nullable = false)
//     private Instant updatedAt;
// }
package com.english.api.content.model;

import com.english.api.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

@Entity
@Table(name = "content_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentComment {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private ContentPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ContentComment parent; // null = root comment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author; // nullable -> ON DELETE SET NULL

    @Column(name = "body_md", columnDefinition = "text", nullable = false)
    private String bodyMd;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            // UUID v7 - time ordered
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
