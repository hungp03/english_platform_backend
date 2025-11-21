package com.english.api.enrollment.dto.request;

import java.util.List;

public record AIStudyPlanContextRequest(
    AIStudyPlanRequest request,
    List<QuizPerformanceData> recentQuizzes,
    List<EnrollmentProgressData> activeEnrollments
) {}
