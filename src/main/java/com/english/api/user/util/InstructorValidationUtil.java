package com.english.api.user.util;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.user.repository.InstructorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InstructorValidationUtil {
    
    private final InstructorProfileRepository instructorProfileRepository;
    
    /**
     * Validates that the current user is an active instructor
     * @throws AccessDeniedException if instructor is not active
     */
    public void validateActiveInstructor() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        validateActiveInstructor(currentUserId);
    }
    
    /**
     * Validates that the specified user is an active instructor
     * @param userId the user ID to check
     * @throws AccessDeniedException if instructor is not active
     */
    public void validateActiveInstructor(UUID userId) {
        boolean isActive = instructorProfileRepository.existsByUserIdAndIsActiveTrue(userId);
        if (!isActive) {
            throw new AccessDeniedException("Instructor account is not active");
        }
    }
}
