package com.english.api.enrollment.dto.request;

import java.util.List;

public record AIStudyPlanContextRequest(
    AIStudyPlanRequest request,
    LearningProfileData learningProfile,
    List<QuizPerformanceData> recentQuizzes,
    List<EnrollmentProgressData> activeEnrollments
) {}
