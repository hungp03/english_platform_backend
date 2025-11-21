package com.english.api.enrollment.dto.request;

public record QuizPerformanceData(
    String quizTitle,
    String skill,
    Double score,
    Double maxScore,
    String status
) {}
