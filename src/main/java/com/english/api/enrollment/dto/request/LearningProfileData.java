package com.english.api.enrollment.dto.request;

public record LearningProfileData(
    String currentLevel,
    String learningGoal,
    Integer targetScore,
    Integer dailyStudyMinutes,
    String preferredStudyTime,
    Integer studyDaysPerWeek
) {}
