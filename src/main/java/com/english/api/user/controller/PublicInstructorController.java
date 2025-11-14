package com.english.api.user.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.response.InstructorStatsResponse;
import com.english.api.course.service.CourseService;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.dto.response.InstructorProfileResponse;
import com.english.api.user.dto.response.PublicInstructorResponse;
import com.english.api.user.model.InstructorProfile;
import com.english.api.user.repository.InstructorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/instructors")
@RequiredArgsConstructor
public class PublicInstructorController {

    private final InstructorProfileRepository instructorProfileRepository;
    private final CourseService courseService;

        @GetMapping("/{userId}")
    public ResponseEntity<PublicInstructorResponse> getPublicInstructorOverview(
            @PathVariable UUID userId
    ) {
        var profile = instructorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor profile not found"));

        var profileDto = new InstructorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getUser().getAvatarUrl(),
                profile.getBio(),
                profile.getExpertise(),
                profile.getExperienceYears(),
                profile.getQualification(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );

        InstructorStatsResponse stats = courseService.getInstructorStats(userId);
        return ResponseEntity.ok(new PublicInstructorResponse(profileDto, stats));
    }

    /**
     * Courses endpoint: ONLY paginated courses of the instructor.
     * GET /api/public/instructors/{userId}/courses
     */
    @GetMapping("/{userId}/courses")
    public ResponseEntity<PaginationResponse> listPublishedCoursesOfInstructor(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String[] skills
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(courseService.getPublishedByInstructor(userId, pageable, keyword, skills));
    }
}
