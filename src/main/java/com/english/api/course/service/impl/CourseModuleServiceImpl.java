package com.english.api.course.service.impl;

import com.english.api.common.exception.DuplicatePositionException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.request.CourseModuleRequest;
import com.english.api.course.dto.request.CourseModuleUpdateRequest;
import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.dto.response.CourseModuleUpdateResponse;
import com.english.api.course.mapper.CourseModuleMapper;
import com.english.api.course.model.Course;
import com.english.api.course.model.CourseModule;
import com.english.api.course.repository.CourseModuleRepository;
import com.english.api.course.repository.CourseRepository;
import com.english.api.course.service.CourseModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 10/4/2025
 */
@Service
@RequiredArgsConstructor
public class CourseModuleServiceImpl implements CourseModuleService {
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseModuleMapper mapper;

    @Override
    @Transactional
    public List<CourseModuleResponse> create(UUID courseId, List<CourseModuleRequest> requests) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // check duplicate positions trong cùng request
        Set<Integer> positions = new HashSet<>();
        for (CourseModuleRequest req : requests) {
            if (!positions.add(req.position())) {
                throw new DuplicatePositionException("Duplicate position " + req.position() + " in request payload");
            }
            if (moduleRepository.existsByCourseIdAndPosition(courseId, req.position())) {
                throw new DuplicatePositionException(
                        "Position " + req.position() + " already exists in course " + courseId
                );
            }
        }

        List<CourseModule> modules = requests.stream()
                .map(req -> {
                    CourseModule module = mapper.toEntity(req);
                    module.setCourse(course);
                    return module;
                })
                .toList();

        moduleRepository.saveAll(modules);
        return modules.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<CourseModuleResponse> list(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return moduleRepository.findModulesWithLessonCount(courseId);
    }

    @Override
    public CourseModuleResponse getById(UUID courseId, UUID moduleId) {
        return moduleRepository.findModuleWithLessonCount(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
    }

    @Override
    @Transactional
    public List<CourseModuleUpdateResponse> update(UUID courseId, List<CourseModuleUpdateRequest> requests) {
        // Check trùng position trong payload
        Set<Integer> seen = new HashSet<>();
        for (CourseModuleUpdateRequest req : requests) {
            if (!seen.add(req.position())) {
                throw new DuplicatePositionException("Duplicate position " + req.position() + " in request payload");
            }
        }

        // Lấy tất cả modules cần update
        List<UUID> ids = requests.stream().map(CourseModuleUpdateRequest::id).toList();
        List<CourseModule> modules = moduleRepository.findAllById(ids);

        if (modules.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more modules not found");
        }

        // Validate position conflict trong DB (ngoài request)
        for (CourseModuleUpdateRequest req : requests) {
            boolean conflict = moduleRepository.existsByCourseIdAndPosition(courseId, req.position())
                               && modules.stream().noneMatch(m -> m.getId().equals(req.id()) && m.getPosition().equals(req.position()));

            if (conflict) {
                throw new DuplicatePositionException(
                        "Position " + req.position() + " already exists in course " + courseId
                );
            }
        }

        // Apply update
        Map<UUID, CourseModuleUpdateRequest> requestMap = requests.stream()
                .collect(Collectors.toMap(CourseModuleUpdateRequest::id, r -> r));

        modules.forEach(m -> {
            CourseModuleUpdateRequest req = requestMap.get(m.getId());
            m.setTitle(req.title());
            m.setPosition(req.position());
        });

        moduleRepository.saveAll(modules);

        return modules.stream().map(mapper::toUpdateResponse).toList();
    }

    @Override
    @Transactional
    public void delete(UUID courseId, UUID moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .filter(m -> m.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        //  Integer deletedPosition = module.getPosition();
        moduleRepository.delete(module);

        // update position
        // moduleRepository.shiftPositionsAfterDelete(courseId, deletedPosition);
    }
}
