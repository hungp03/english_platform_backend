package com.english.api.forum.mapper;

import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.repo.ForumPostRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class ForumReportMapper {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ForumPostRepository postRepository;

    @Autowired
    protected ForumThreadRepository threadRepository;

    @Mapping(target = "reporterName", ignore = true)
    @Mapping(target = "reporterEmail", ignore = true)
    @Mapping(target = "targetPreview", ignore = true)
    @Mapping(target = "targetPublished", ignore = true)
    public abstract ForumReportResponse toResponse(ForumReport report);

    @Mapping(target = "reporterName", expression = "java(extractReporterName(report, userMap))")
    @Mapping(target = "reporterEmail", expression = "java(extractReporterEmail(report, userMap))")
    @Mapping(target = "targetPreview", expression = "java(extractTargetPreview(report, postMap, threadMap))")
    @Mapping(target = "targetPublished", expression = "java(extractTargetPublished(report, postMap, threadMap))")
    public abstract ForumReportResponse toResponse(ForumReport report,
                                                   @Context Map<UUID, User> userMap,
                                                   @Context Map<UUID, ForumPost> postMap,
                                                   @Context Map<UUID, ForumThread> threadMap);

    public ForumReportResponse toResponseWithFetch(ForumReport report) {
        Map<UUID, User> userMap = Collections.emptyMap();
        Map<UUID, ForumPost> postMap = Collections.emptyMap();
        Map<UUID, ForumThread> threadMap = Collections.emptyMap();

        if (report.getUserId() != null) {
            var user = userRepository.findById(report.getUserId()).orElse(null);
            if (user != null) {
                userMap = Map.of(user.getId(), user);
            }
        }

        if (report.getTargetType() == ReportTargetType.POST) {
            var post = postRepository.findById(report.getTargetId()).orElse(null);
            if (post != null) {
                postMap = Map.of(post.getId(), post);
            }
        } else if (report.getTargetType() == ReportTargetType.THREAD) {
            var thread = threadRepository.findById(report.getTargetId()).orElse(null);
            if (thread != null) {
                threadMap = Map.of(thread.getId(), thread);
            }
        }

        return toResponse(report, userMap, postMap, threadMap);
    }

    protected String extractReporterName(ForumReport report, Map<UUID, User> userMap) {
        if (report.getUserId() == null) {
            return null;
        }
        User user = userMap.get(report.getUserId());
        return user != null ? user.getFullName() : null;
    }

    protected String extractReporterEmail(ForumReport report, Map<UUID, User> userMap) {
        if (report.getUserId() == null) {
            return null;
        }
        User user = userMap.get(report.getUserId());
        return user != null ? user.getEmail() : null;
    }

    protected String extractTargetPreview(ForumReport report, Map<UUID, ForumPost> postMap, Map<UUID, ForumThread> threadMap) {
        if (report.getTargetType() == ReportTargetType.POST) {
            ForumPost post = postMap.get(report.getTargetId());
            return post != null ? post.getBodyMd() : null;
        } else if (report.getTargetType() == ReportTargetType.THREAD) {
            ForumThread thread = threadMap.get(report.getTargetId());
            return thread != null ? thread.getTitle() : null;
        }
        return null;
    }

    protected Boolean extractTargetPublished(ForumReport report, Map<UUID, ForumPost> postMap, Map<UUID, ForumThread> threadMap) {
        if (report.getTargetType() == ReportTargetType.POST) {
            ForumPost post = postMap.get(report.getTargetId());
            return post != null ? post.isPublished() : null;
        } else if (report.getTargetType() == ReportTargetType.THREAD) {
            ForumThread thread = threadMap.get(report.getTargetId());
            return thread != null ? !thread.isLocked() : null;
        }
        return null;
    }
}
