package com.english.api.user.dto.response;

import com.english.api.user.model.enums.EnglishLevel;
import com.english.api.user.model.enums.LearningGoal;
import com.english.api.user.model.enums.PreferredStudyTime;

public record LearningProfileResponse(
    EnglishLevel currentLevel,
    LearningGoal learningGoal,
    Integer targetScore,
    Integer dailyStudyMinutes,
    PreferredStudyTime preferredStudyTime,
    Integer studyDaysPerWeek
) {}
