package com.english.api.course.controller;

import com.english.api.course.dto.request.SkillRequest;
import com.english.api.course.dto.response.SkillResponse;
import com.english.api.course.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        SkillResponse skill = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(skill);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> updateSkill(@PathVariable UUID id, @Valid @RequestBody SkillRequest request) {
        SkillResponse skill = skillService.updateSkill(id, request);
        return ResponseEntity.ok(skill);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSkill(@PathVariable UUID id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
