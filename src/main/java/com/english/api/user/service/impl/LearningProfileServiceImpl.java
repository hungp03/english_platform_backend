package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.user.dto.request.UpdateLearningProfileRequest;
import com.english.api.user.dto.response.LearningProfileResponse;
import com.english.api.user.mapper.LearningProfileMapper;
import com.english.api.user.model.LearningProfile;
import com.english.api.user.model.User;
import com.english.api.user.repository.LearningProfileRepository;
import com.english.api.user.service.LearningProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LearningProfileServiceImpl implements LearningProfileService {

    private final LearningProfileRepository repository;
    private final LearningProfileMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public LearningProfileResponse getMyProfile() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return repository.findByUserId(userId)
                .map(mapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public LearningProfileResponse updateProfile(UpdateLearningProfileRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        LearningProfile profile = repository.findByUserId(userId)
                .orElseGet(() -> LearningProfile.builder()
                        .user(User.builder().id(userId).build())
                        .build());

        if (request.currentLevel() != null) profile.setCurrentLevel(request.currentLevel());
        if (request.learningGoal() != null) profile.setLearningGoal(request.learningGoal());
        if (request.targetScore() != null) profile.setTargetScore(request.targetScore());
        if (request.dailyStudyMinutes() != null) profile.setDailyStudyMinutes(request.dailyStudyMinutes());
        if (request.preferredStudyTime() != null) profile.setPreferredStudyTime(request.preferredStudyTime());
        if (request.studyDaysPerWeek() != null) profile.setStudyDaysPerWeek(request.studyDaysPerWeek());

        return mapper.toResponse(repository.save(profile));
    }

    @Override
    @Transactional
    public void deleteProfile() {
        UUID userId = SecurityUtil.getCurrentUserId();
        repository.findByUserId(userId).ifPresent(repository::delete);
    }
}
