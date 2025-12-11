package com.english.api.course.service;

import com.english.api.course.dto.request.SkillRequest;
import com.english.api.course.dto.response.SkillResponse;
import java.util.List;
import java.util.UUID;

public interface SkillService {
    List<SkillResponse> getAllSkills();
    SkillResponse createSkill(SkillRequest request);
    SkillResponse updateSkill(UUID id, SkillRequest request);
    void deleteSkill(UUID id);
}
