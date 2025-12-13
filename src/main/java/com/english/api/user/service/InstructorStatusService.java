package com.english.api.user.service;

import com.english.api.user.repository.InstructorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorStatusService {
    
    private final InstructorProfileRepository instructorProfileRepository;
    
    @Cacheable(value = "instructorStatus", key = "#userId")
    public boolean isInstructorActive(UUID userId) {
        return instructorProfileRepository.existsByUserIdAndIsActiveTrue(userId);
    }
}
