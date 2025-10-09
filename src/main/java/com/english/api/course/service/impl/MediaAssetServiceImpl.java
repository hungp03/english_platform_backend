package com.english.api.course.service.impl;

import com.english.api.course.dto.response.MediaAssetResponse;
import com.english.api.course.mapper.MediaAssetMapper;
import com.english.api.course.model.MediaAsset;
import com.english.api.course.repository.MediaAssetRepository;
import com.english.api.course.service.MediaAssetService;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by hungpham on 10/7/2025
 */
@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetRepository assetRepository;
    private final S3AsyncClient s3AsyncClient;
    private final MediaAssetMapper mapper;

    @Value("${cloud.bucket}")
    private String bucket;

    @Override
    public CompletableFuture<MediaAssetResponse> upload(MultipartFile file) {
        User currentUser = getCurrentUser();
        String key = "course/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            // Tạo request upload
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // Gửi request bất đồng bộ
            CompletableFuture<PutObjectResponse> uploadFuture = s3AsyncClient.putObject(
                    request,
                    AsyncRequestBody.fromBytes(file.getBytes())
            );

            // Khi upload xong thì lưu DB
            return uploadFuture.thenApply(result -> {
                String url = s3AsyncClient.utilities()
                        .getUrl(GetUrlRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build())
                        .toExternalForm();

                MediaAsset asset = MediaAsset.builder()
                        .owner(currentUser)
                        .mimeType(file.getContentType())
                        .url(url)
                        .meta(null)
                        .createdAt(Instant.now())
                        .build();

                return mapper.toResponse(assetRepository.save(asset));
            }).exceptionally(ex -> {
                throw new RuntimeException("Failed to upload file", ex);
            });

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes", e);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new RuntimeException("User not authenticated");
        }
        return user;
    }
}
