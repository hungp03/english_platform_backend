package com.english.api.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorCacheUtil {
    /**
     * Clear instructor status cache when status changes
     */
    @CacheEvict(value = "instructorStatus", key = "#userId")
    public void clearInstructorStatusCache(UUID userId) {
        // Cache will be cleared automatically
    }
}
