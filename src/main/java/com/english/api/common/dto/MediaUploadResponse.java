package com.english.api.common.dto;

/**
 * Created by hungpham on 10/5/2025
 */
public record MediaUploadResponse(
        String fileName,
        String url,
        long size,
        String contentType
) {
}
