package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Enrollment entity
 * Created by hungpham on 10/29/2025
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
}
