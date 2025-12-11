package com.english.api.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SkillRequest {
    @NotBlank(message = "Skill name is required")
    @Size(max = 255, message = "Skill name must not exceed 255 characters")
    private String name;
}
