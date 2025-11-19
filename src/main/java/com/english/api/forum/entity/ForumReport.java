package com.english.api.forum.entity;

import com.english.api.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "forum_reports", indexes = {
    @Index(name = "idx_forum_reports_user", columnList = "user_id"),
    @Index(name = "idx_forum_reports_created_at", columnList = "created_at")
})
public class ForumReport {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 16, nullable = false)
    private ReportTargetType targetType;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    // --- THAY ĐỔI TẠI ĐÂY: Liên kết trực tiếp với User ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    // --- THAY ĐỔI TẠI ĐÂY: Liên kết người giải quyết (Admin) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
}