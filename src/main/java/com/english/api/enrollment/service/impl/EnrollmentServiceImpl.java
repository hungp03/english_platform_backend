package com.english.api.enrollment.service.impl;

import com.english.api.enrollment.repository.EnrollmentRepository;
import com.english.api.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of EnrollmentService
 * Created by hungpham on 10/29/2025
 */
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
}
