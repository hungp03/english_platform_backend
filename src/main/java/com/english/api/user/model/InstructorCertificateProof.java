package com.english.api.user.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * InstructorCertificateProof entity - Stores certificate/qualification proofs for instructor requests
 * Created by hungpham on 10/30/2025
 */
@Entity
@Table(
    name = "instructor_certificate_proofs",
    indexes = {
        @Index(name = "idx_certificate_proofs_request", columnList = "request_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorCertificateProof {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, referencedColumnName = "id")
    private InstructorRequest instructorRequest;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
