package com.english.api.course.service;

import com.english.api.course.dto.response.MediaAssetResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * Created by hungpham on 10/7/2025
 */
public interface MediaAssetService {
    CompletableFuture<MediaAssetResponse> upload(MultipartFile file);
}
