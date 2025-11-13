package com.english.api.forum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "forum_reports")
public class ForumReport {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) private UUID id;
    @Enumerated(EnumType.STRING) @Column(name = "target_type", length = 16, nullable = false) private ReportTargetType targetType;
    @JdbcTypeCode(SqlTypes.UUID) @Column(name = "target_id", nullable = false) private UUID targetId;
    @JdbcTypeCode(SqlTypes.UUID) @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "reason", nullable = false, columnDefinition = "text") private String reason;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @JdbcTypeCode(SqlTypes.UUID) @Column(name = "resolved_by") private UUID resolvedBy;
}
