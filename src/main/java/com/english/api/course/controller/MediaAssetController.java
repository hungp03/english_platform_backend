package com.english.api.course.controller;

import com.english.api.course.dto.response.MediaAssetResponse;
import com.english.api.course.service.MediaAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by hungpham on 10/7/2025
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaAssetController {

    private final MediaAssetService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaAssetResponse> upload(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(mediaService.upload(file).join());
    }
}
