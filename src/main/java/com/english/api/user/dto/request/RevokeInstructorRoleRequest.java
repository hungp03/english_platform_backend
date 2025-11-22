package com.english.api.user.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for managing instructor role (revoke or restore)
 * Created by hungpham on 11/22/2025
 */
public record RevokeInstructorRoleRequest(
        @NotNull(message = "Action is required")
        Action action,
        
        @NotBlank(message = "Reason is required")
        String reason
) {
    public enum Action {
        REVOKE,
        RESTORE
    }
}
