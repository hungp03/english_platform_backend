package com.english.api.enrollment.controller;

import com.english.api.enrollment.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for LessonProgress operations
 * Created by hungpham on 10/29/2025
 */
@RestController
@RequestMapping("/api/lesson-progress")
@RequiredArgsConstructor
public class LessonProgressController {
    private final LessonProgressService lessonProgressService;
}
