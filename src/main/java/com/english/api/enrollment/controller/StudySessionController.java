package com.english.api.enrollment.controller;

import com.english.api.enrollment.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for StudySession operations
 * Created by hungpham on 10/29/2025
 */
@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {
    private final StudySessionService studySessionService;
}
