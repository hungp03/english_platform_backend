package com.english.api.user.dto.request;

import com.english.api.user.model.enums.EnglishLevel;
import com.english.api.user.model.enums.LearningGoal;
import com.english.api.user.model.enums.PreferredStudyTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateLearningProfileRequest(
    EnglishLevel currentLevel,
    LearningGoal learningGoal,
    @Min(0) @Max(990) Integer targetScore,
    @Min(10) @Max(480) Integer dailyStudyMinutes,
    PreferredStudyTime preferredStudyTime,
    @Min(1) @Max(7) Integer studyDaysPerWeek
) {}
