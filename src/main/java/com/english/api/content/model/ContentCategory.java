// package com.english.api.content.model;

// import jakarta.persistence.*;
// import lombok.*;
// import org.hibernate.annotations.CreationTimestamp;

// import java.time.Instant;
// import java.util.UUID;

// @Entity
// @Table(name = "content_categories",
//        uniqueConstraints = {
//            @UniqueConstraint(name = "uk_content_categories_slug", columnNames = {"slug"})
//        })
// @Getter @Setter
// @NoArgsConstructor @AllArgsConstructor @Builder
// public class ContentCategory {

//     @Id
//     @GeneratedValue
//     @Column(columnDefinition = "uuid")
//     // @GeneratedValue(strategy = GenerationType.UUID) // <-- cách 1 (chuẩn JPA mới)
//   // hoặc: @UuidGenerator // <-- cách 2 (annotation Hibernate)
//     // @Column(columnDefinition = "uuid", updatable = false, nullable = false)
//     private UUID id;

//     @Column(nullable = false)
//     private String name;

//     @Column(nullable = false, unique = true)
//     private String slug;

//     @Column(columnDefinition = "text")
//     private String description;

//     @CreationTimestamp
//     @Column(name = "created_at", nullable = false, updatable = false)
//     private Instant createdAt;
// }
package com.english.api.content.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;
import com.github.f4b6a3.uuid.UuidCreator;

@Entity
@Table(
    name = "content_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_categories_slug", columnNames = {"slug"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentCategory {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
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

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            // Sinh UUID v7 theo thứ tự thời gian
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
