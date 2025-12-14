package com.english.api.user.service;

import com.english.api.user.dto.request.UpdateLearningProfileRequest;
import com.english.api.user.dto.response.LearningProfileResponse;

public interface LearningProfileService {
    LearningProfileResponse getMyProfile();
    LearningProfileResponse updateProfile(UpdateLearningProfileRequest request);
    void deleteProfile();
}
