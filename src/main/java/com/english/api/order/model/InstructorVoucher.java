package com.english.api.order.model;

import com.english.api.course.model.Course;
import com.english.api.order.model.enums.VoucherDiscountType;
import com.english.api.order.model.enums.VoucherScope;
import com.english.api.order.model.enums.VoucherStatus;
import com.english.api.user.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "instructor_vouchers",
    indexes = {
        @Index(name = "idx_vouchers_instructor", columnList = "instructor_id"),
        @Index(name = "idx_vouchers_code", columnList = "code"),
        @Index(name = "idx_vouchers_status", columnList = "status")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorVoucher {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VoucherScope scope = VoucherScope.SPECIFIC_COURSES;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private VoucherDiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_per_user")
    @Builder.Default
    private Integer usagePerUser = 1;

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "instructor_voucher_courses",
        joinColumns = @JoinColumn(name = "voucher_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private Set<Course> applicableCourses = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    public boolean isValid() {
        OffsetDateTime now = OffsetDateTime.now();
        return status == VoucherStatus.ACTIVE
                && now.isAfter(startDate)
                && now.isBefore(endDate)
                && (usageLimit == null || usedCount < usageLimit);
    }

    public boolean isApplicableToCourse(UUID courseId) {
        if (scope == VoucherScope.ALL_INSTRUCTOR_COURSES) {
            return true;
        }
        return applicableCourses.stream()
                .anyMatch(course -> course.getId().equals(courseId));
    }
}
