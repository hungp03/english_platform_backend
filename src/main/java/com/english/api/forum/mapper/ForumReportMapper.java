package com.english.api.forum.mapper;

import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.model.ForumPost;
import com.english.api.forum.model.ForumReport;
import com.english.api.forum.model.ForumThread;
import com.english.api.forum.model.ReportTargetType;
import com.english.api.forum.repository.ForumPostRepository;
import com.english.api.forum.repository.ForumThreadRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ForumReportMapper {

    @Autowired
    protected ForumPostRepository postRepository;

    @Autowired
    protected ForumThreadRepository threadRepository;

    @Mapping(target = "userId", source = "report.user.id")
    @Mapping(target = "reporterName", source = "report.user.fullName")
    @Mapping(target = "reporterEmail", source = "report.user.email")
    @Mapping(target = "resolvedBy", expression = "java(report.getResolvedBy() != null ? report.getResolvedBy().getFullName() : null)")
    @Mapping(target = "targetPreview", expression = "java(extractTargetPreview(report))")
    @Mapping(target = "targetPublished", expression = "java(extractTargetPublished(report))")
    public abstract ForumReportResponse toResponse(ForumReport report);

    protected String extractTargetPreview(ForumReport report) {
        if (report.getTargetType() == ReportTargetType.POST) {
            ForumPost post = postRepository.findById(report.getTargetId()).orElse(null);
            return post != null ? post.getBodyMd() : null;
        } else if (report.getTargetType() == ReportTargetType.THREAD) {
            ForumThread thread = threadRepository.findById(report.getTargetId()).orElse(null);
            return thread != null ? thread.getTitle() : null;
        }
        return null;
    }

    protected Boolean extractTargetPublished(ForumReport report) {
        if (report.getTargetType() == ReportTargetType.POST) {
            ForumPost post = postRepository.findById(report.getTargetId()).orElse(null);
            return post != null ? post.isPublished() : null;
        } else if (report.getTargetType() == ReportTargetType.THREAD) {
            ForumThread thread = threadRepository.findById(report.getTargetId()).orElse(null);
            return thread != null ? !thread.isLocked() : null;
        }
        return null;
    }
}
