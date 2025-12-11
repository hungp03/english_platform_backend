package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.util.SlugUtil;
import com.english.api.course.dto.request.MediaCallbackRequest;
import com.english.api.course.dto.response.MediaAssetResponse;
import com.english.api.course.dto.response.MediaAssetSimpleResponse;
import com.english.api.course.mapper.MediaAssetMapper;
import com.english.api.course.model.MediaAsset;
import com.english.api.course.repository.MediaAssetRepository;
import com.english.api.course.service.MediaAssetService;
import com.english.api.course.util.MediaTokenUtil;
import com.english.api.user.model.User;
import com.english.api.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.english.api.common.service.MediaService;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetRepository assetRepository;
    private final UserService userService;
    private final MediaAssetMapper mediaAssetMapper;
    private final MediaService mediaService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.worker.url:http://localhost:10000/upload}")
    private String workerUrl;

    @Value("${app.worker.api-token}")
    private String workerApiToken;

    @Value("${app.worker.callback-secret}")
    private String callbackSecret;

    @Value("${app.media.secret-token}")
    private String mediaSecret;

    @Override
    @Transactional
    public MediaAssetResponse uploadVideo(MultipartFile file) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);

        // Kiểm tra định dạng video
        String mimeType = file.getContentType();
        if (mimeType == null || !mimeType.startsWith("video/")) {
            throw new IllegalArgumentException("Only video formats accepted");
        }

        // Tạo MediaAsset với trạng thái "processing"
        MediaAsset asset = MediaAsset.builder()
                .owner(user)
                .mimeType(mimeType)
                .createdAt(Instant.now())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode meta = mapper.createObjectNode();
        meta.put("filename", file.getOriginalFilename());
        meta.put("size", file.getSize());
        meta.put("status", "processing");
        meta.put("uploadedAt", Instant.now().toString());
        asset.setMeta(meta);

        asset = assetRepository.save(asset);

        // Lưu file tạm
        String safeFilename = Objects.requireNonNull(file.getOriginalFilename())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "videos");
        Path tempFile = tempDir.resolve(asset.getId().toString() + "-" + safeFilename);

        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                log.info("Created temp directory: {}", tempDir.toAbsolutePath());
            }

            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved temp file: {}", tempFile.toAbsolutePath());

            // Gửi sang worker qua HTTP
            sendToWorker(
                    asset.getId(),
                    tempFile,
                    userId,
                    SlugUtil.toSlugWithUuid(file.getOriginalFilename())
            );

        } catch (IOException e) {
            log.error("Failed to save temp file", e);
            throw new RuntimeException("Failed to save temp file", e);

        } finally {
            // Dọn dẹp file tạm (ngầm, không crash nếu lỗi)
            try {
                if (Files.exists(tempFile)) {
                    Files.delete(tempFile);
                    log.info("Deleted temp file: {}", tempFile);
                }
            } catch (IOException ex) {
                log.warn("Could not delete temp file: {}", tempFile);
            }
        }

        // Trả kết quả tạm thời cho frontend
        return mediaAssetMapper.toResponse(asset);
    }

    @Override
    @Transactional
    public MediaAssetResponse uploadAttachment(MultipartFile file, String title) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);

        String mimeType = file.getContentType();
        if (mimeType == null) {
            throw new IllegalArgumentException("Unknown file type");
        }

        // Upload file lên S3 trước
        com.english.api.common.dto.MediaUploadResponse uploadResponse;
        try {
            uploadResponse = mediaService.uploadFile(file, "lesson_attachments");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        // Tạo MediaAsset trong database
        MediaAsset asset = MediaAsset.builder()
                .owner(user)
                .mimeType(mimeType)
                .url(uploadResponse.url())
                .title(title)
                .createdAt(Instant.now())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode meta = mapper.createObjectNode();
        meta.put("originalName", file.getOriginalFilename());
        meta.put("size", file.getSize());
        meta.put("status", "ready");
        meta.put("uploadedAt", Instant.now().toString());
        asset.setMeta(meta);

        asset = assetRepository.save(asset);
        return mediaAssetMapper.toResponse(asset);
    }

    @Transactional
    @Override
    public void handleWorkerCallback(HttpServletRequest request, String signature) {
        try {
            // Đọc body thô
            String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Tính lại HMAC
            String computed = computeHmac(body, callbackSecret);

            if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8),
                    computed.getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("Invalid HMAC signature");
            }

            // Parse JSON và cập nhật asset
            ObjectMapper mapper = new ObjectMapper();
            MediaCallbackRequest req = mapper.readValue(body, MediaCallbackRequest.class);

            assetRepository.findById(req.assetId()).ifPresentOrElse(asset -> {
                asset.setUrl(req.url());
                asset.setMimeType(req.mimeType());
                ObjectNode meta = mapper.createObjectNode();
                meta.put("status", "done");
                meta.put("lessonName", req.lessonName());
                meta.put("duration", req.duration());
                meta.put("updatedAt", Instant.now().toString());
                asset.setMeta(meta);
                assetRepository.save(asset);
                log.info("Updated asset {} successfully", req.assetId());
            }, () -> log.warn("Asset not found for callback: {}", req.assetId()));

        } catch (IOException e) {
            throw new RuntimeException("Failed to read callback body", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public String getSignedMediaUrl(UUID assetId) {
        // UUID userId = SecurityUtil.getCurrentUserId();

        MediaAssetSimpleResponse asset = assetRepository.findVideoById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found"));

        // Kiểm tra quyền sở hữu
        // if (!asset.ownerId().equals(userId)) {
        //     throw new AccessDeniedException("You do not have access to this video");
        // }

        // Parse meta JSON để đọc trạng thái và duration
        ObjectNode meta = (ObjectNode) asset.meta();
        String status = meta.path("status").asText("");
        if (!"done".equalsIgnoreCase(status)) {
            throw new ResourceInvalidException("Video is being processed, please try again later");
        }

        double duration = meta.path("duration").asDouble(0.0);

        // Tính thời hạn token: thời lượng video + 10 phút
        long expireSeconds = (long) Math.ceil(duration) + 600;
        long exp = Instant.now().plusSeconds(expireSeconds).getEpochSecond();

        // Payload token
        String key = extractS3KeyFromUrl(asset.url());

        Map<String, Object> payload = Map.of(
                "sub", key,
                "exp", exp
        );

        String token = MediaTokenUtil.createSignedToken(payload, mediaSecret);
        String signedUrl = asset.url() + "?token=" + token;
        return signedUrl;
    }

    private String extractS3KeyFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            throw new RuntimeException("Invalid video URL", e);
        }
    }

    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new RuntimeException("HMAC compute failed", e);
        }
    }

    private void sendToWorker(UUID assetId, Path tempFile, UUID userId, String lessonName) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("lessonName", lessonName);
        form.add("userId", userId.toString());
        form.add("assetId", assetId.toString());
        form.add("video", new FileSystemResource(tempFile.toFile()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + workerApiToken);

        try {
            restTemplate.postForEntity(workerUrl, new HttpEntity<>(form, headers), String.class);
            log.info("Sent job to worker for asset {}", assetId);
        } catch (Exception e) {
            log.error("Failed to dispatch worker job: {}", e.getMessage());
            throw new RuntimeException("Worker upload failed", e);
        }
    }
}

