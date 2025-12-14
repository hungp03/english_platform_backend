package com.english.api.user.controller;

import com.english.api.user.dto.request.UpdateLearningProfileRequest;
import com.english.api.user.dto.response.LearningProfileResponse;
import com.english.api.user.service.LearningProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning-profile")
@RequiredArgsConstructor
public class LearningProfileController {

    private final LearningProfileService service;

    @GetMapping
    public ResponseEntity<LearningProfileResponse> getMyProfile() {
        return ResponseEntity.ok(service.getMyProfile());
    }

    @PutMapping
    public ResponseEntity<LearningProfileResponse> updateProfile(
            @Valid @RequestBody UpdateLearningProfileRequest request) {
        return ResponseEntity.ok(service.updateProfile(request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile() {
        service.deleteProfile();
        return ResponseEntity.noContent().build();
    }
}
