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
    CourseModuleResponse create(UUID courseId, CourseModuleRequest requests);

    List<CourseModuleResponse> list(UUID courseId);

    List<CourseModuleResponse> listPublished(UUID courseId);

    CourseModuleResponse getById(UUID courseId, UUID moduleId);

    CourseModuleUpdateResponse update(UUID courseId, CourseModuleUpdateRequest requests);

    void delete(UUID courseId, UUID moduleId);

    CourseModuleResponse publish(UUID courseId, UUID moduleId, boolean publish);
}
