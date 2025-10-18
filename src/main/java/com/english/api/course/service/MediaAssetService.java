package com.english.api.course.service;

import com.english.api.course.dto.response.MediaAssetResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
public interface MediaAssetService {

    MediaAssetResponse uploadVideo(MultipartFile file);

    void handleWorkerCallback(HttpServletRequest request, String signature);

    @Transactional(readOnly = true)
    String getSignedMediaUrl(UUID assetId);
}
