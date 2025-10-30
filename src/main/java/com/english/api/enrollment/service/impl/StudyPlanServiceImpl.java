package com.english.api.enrollment.service.impl;

import com.english.api.enrollment.repository.StudyPlanRepository;
import com.english.api.enrollment.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of StudyPlanService
 * Created by hungpham on 10/29/2025
 */
@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {
    private final StudyPlanRepository studyPlanRepository;
}
