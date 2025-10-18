package com.english.api.course.dto.request;
import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record MediaCallbackRequest(
        UUID assetId,
        String lessonName,
        String url,
        String mimeType,
        Double duration
) {}