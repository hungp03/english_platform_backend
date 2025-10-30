package com.english.api.enrollment.service.impl;

import com.english.api.enrollment.repository.StudySessionRepository;
import com.english.api.enrollment.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of StudySessionService
 * Created by hungpham on 10/29/2025
 */
@Service
@RequiredArgsConstructor
public class StudySessionServiceImpl implements StudySessionService {
    private final StudySessionRepository studySessionRepository;
}
