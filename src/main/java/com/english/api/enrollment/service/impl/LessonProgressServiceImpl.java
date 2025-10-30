package com.english.api.enrollment.service.impl;

import com.english.api.enrollment.repository.LessonProgressRepository;
import com.english.api.enrollment.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of LessonProgressService
 * Created by hungpham on 10/29/2025
 */
@Service
@RequiredArgsConstructor
public class LessonProgressServiceImpl implements LessonProgressService {
    private final LessonProgressRepository lessonProgressRepository;
}
