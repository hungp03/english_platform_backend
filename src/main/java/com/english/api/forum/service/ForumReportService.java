package com.english.api.forum.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.entity.ReportTargetType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ForumReportService {
    ForumReportResponse create(ForumReportCreateRequest req);

    PaginationResponse list(ReportTargetType type, boolean onlyOpen, Pageable pageable);

    ForumReportResponse resolve(UUID reportId);
}
