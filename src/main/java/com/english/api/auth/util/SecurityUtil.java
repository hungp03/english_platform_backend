package com.english.api.auth.util;

import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Created by hungpham on 9/24/2025
 */
public class SecurityUtil {
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new ResourceInvalidException("Invalid user ID format.");
        }
    }

}
