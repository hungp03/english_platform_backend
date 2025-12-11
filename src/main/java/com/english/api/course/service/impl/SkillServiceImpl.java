package com.english.api.course.service.impl;

import com.english.api.course.dto.request.SkillRequest;
import com.english.api.course.dto.response.SkillResponse;
import com.english.api.course.model.Skill;
import com.english.api.course.repository.SkillRepository;
import com.english.api.course.service.SkillService;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;

    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName()))
                .toList();
    }

    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Skill already exists: " + request.getName());
        }

        Skill skill = new Skill();
        skill.setId(UUID.randomUUID());
        skill.setName(request.getName().trim());
        skill.setCreatedAt(Instant.now());

        skill = skillRepository.save(skill);
        return new SkillResponse(skill.getId(), skill.getName());
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(UUID id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (!skill.getName().equalsIgnoreCase(request.getName()) && 
            skillRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Skill already exists: " + request.getName());
        }

        skill.setName(request.getName().trim());
        skill = skillRepository.save(skill);
        return new SkillResponse(skill.getId(), skill.getName());
    }

    @Override
    @Transactional
    public void deleteSkill(UUID id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found");
        }
        
        if (skillRepository.isSkillUsedByCourses(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete skill that is being used by courses");
        }
        
        skillRepository.deleteById(id);
    }
}
