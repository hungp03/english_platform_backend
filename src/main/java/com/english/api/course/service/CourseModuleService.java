package com.english.api.course.service;

import com.english.api.course.dto.request.CourseModuleRequest;
import com.english.api.course.dto.request.CourseModuleUpdateRequest;
import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.dto.response.CourseModuleUpdateResponse;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public interface CourseModuleService {
    List<CourseModuleResponse> create(UUID courseId, List<CourseModuleRequest> requests);

    List<CourseModuleResponse> list(UUID courseId);

    CourseModuleResponse getById(UUID courseId, UUID moduleId);

    List<CourseModuleUpdateResponse> update(UUID courseId, List<CourseModuleUpdateRequest> requests);

    void delete(UUID courseId, UUID moduleId);
}
