package com.english.api.user.dto.request;

import com.english.api.common.util.constant.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by hungpham on 10/1/2025
 */
public record UpdateUserRequest(
        @Pattern(
                regexp = ValidationPatterns.FULL_NAME_PATTERN,
                message = "Full name must not contain numbers or special characters"
        )
        String fullName,

        @Email(message = "Invalid email format")
        @Pattern(
                regexp = ValidationPatterns.EMAIL_PATTERN,
                message = "Email cannot contain special characters or spaces, only -, _, ., and + are allowed"
        )
        String email,

        String avatarUrl
        // MultipartFile avatarFile
) {}


