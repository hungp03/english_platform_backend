package com.english.api.course.controller;

import com.english.api.course.dto.response.MediaAssetResponse;
import com.english.api.course.service.MediaAssetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaAssetController {

    private final MediaAssetService mediaService;

    @PostMapping("/upload-course-video")
    public ResponseEntity<MediaAssetResponse> upload(@RequestParam("file") MultipartFile file) {
            MediaAssetResponse response = mediaService.uploadVideo(file);
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED) // 202 Accepted → async processing
                    .body(response);
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> handleWorkerCallback(
            HttpServletRequest request,
            @RequestHeader("X-Signature") String signature
    ) {
        try {
            mediaService.handleWorkerCallback(request, signature);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sign-video/{mediaId}")
    public ResponseEntity<Map<String, String>> signVideo(@PathVariable UUID mediaId) {
        String signedUrl = mediaService.getSignedMediaUrl(mediaId);
        return ResponseEntity.ok(Map.of("signedUrl", signedUrl));
    }
}
