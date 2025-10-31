package com.english.api.common.service.impl;

import com.english.api.common.dto.MediaUploadResponse;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 10/5/2025
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private final S3AsyncClient s3Client;

    @Value("${cloud.public-url}")
    private String publicUrl;

    @Value("${cloud.bucket}")
    private String bucket;

    // Loại MIME hợp lệ (toàn cục)
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "image/bmp",
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/x-wav", "audio/mp4", "audio/webm",
            "application/pdf"
    );

    // Chỉ các loại ảnh (dùng cho folder users)
    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "image/bmp"
    );

    private static final Set<String> PROOF_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf"
    );

    private static final Map<String, FolderRule> FOLDER_RULES = Map.of(
            "users", new FolderRule(2 * 1024 * 1024, IMAGE_MIME_TYPES), // 2MB, chỉ ảnh
            "certificate_proofs", new FolderRule(5 * 1024 * 1024, PROOF_MIME_TYPES),
            "forums", new FolderRule(50 * 1024 * 1024, ALLOWED_MIME_TYPES),// 50MB, tất cả loại hợp lệ
            "course_thumbnail", new FolderRule(2 * 1024 * 1024, IMAGE_MIME_TYPES) // 2MB, chỉ ảnh
    );

    // Inner class chứa rule cho từng folder
    private static class FolderRule {
        long maxSize;
        Set<String> allowedTypes;

        FolderRule(long maxSize, Set<String> allowedTypes) {
            this.maxSize = maxSize;
            this.allowedTypes = allowedTypes;
        }
    }


    @Override
    public MediaUploadResponse uploadFile(MultipartFile file, String folder) throws IOException {
        // Kiểm tra cơ bản
        if (file.isEmpty() || file.getSize() == 0) {
            throw new ResourceInvalidException("Uploaded file is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ResourceInvalidException("File name is missing or invalid.");
        }

        if (folder == null || folder.isBlank()) {
            throw new ResourceInvalidException("Folder is required.");
        }
        if (folder.contains("..")) {
            throw new ResourceInvalidException("Invalid folder path.");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ResourceInvalidException("Unknown content type.");
        }


        // Xác định quy tắc theo thư mục upload
        FolderRule rule = FOLDER_RULES.entrySet().stream()
                .filter(e -> folder.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() ->
                        new ResourceInvalidException("Unknown or unauthorized upload folder: " + folder)
                );

        // Kiểm tra loại MIME
        if (!rule.allowedTypes.contains(contentType)) {
            throw new ResourceInvalidException(String.format(
                    "File type '%s' is not allowed in folder '%s'.", contentType, folder
            ));
        }

        // Kiểm tra dung lượng tối đa
        if (file.getSize() > rule.maxSize) {
            throw new ResourceInvalidException(String.format(
                    "File size exceeds the limit for folder '%s' (%.2f MB max).",
                    folder, rule.maxSize / 1024.0 / 1024.0
            ));
        }

        // Kiểm tra phần mở rộng file
        if (!originalFilename.matches("(?i).+\\.(png|jpe?g|gif|webp|bmp|mp3|wav|ogg|mp4|webm|pdf)$")) {
            throw new ResourceInvalidException("Unsupported file extension.");
        }


        // Upload lên S3
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + extension;
        String key = String.format("%s/%s",
                folder.replaceAll("^/+", "").replaceAll("/+$", ""),
                filename
        );

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        CompletableFuture<PutObjectResponse> future =
                s3Client.putObject(req, AsyncRequestBody.fromBytes(file.getBytes()));

        future.join();

        String url = String.join("/", publicUrl.replaceAll("/+$", ""), key);

        return new MediaUploadResponse(
                filename,
                url,
                file.getSize(),
                contentType
        );
    }

    @Override
    public List<MediaUploadResponse> uploadFiles(List<MultipartFile> files, String folder) throws IOException {
        List<CompletableFuture<MediaUploadResponse>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadFile(file, folder);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

//    @Override
//    public void deleteFile(String key) {
//        try {
//            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(key)
//                    .build();
//
//            CompletableFuture<DeleteObjectResponse> future = s3Client.deleteObject(deleteRequest);
//
//            // Chờ hoàn tất (vì đang dùng Tomcat → blocking model)
//            future.join();
//
//        } catch (CompletionException e) {
//            // bắt lỗi từ future
//            if (e.getCause() instanceof S3Exception) {
//                throw new CannotDeleteException("Failed to delete file from S3: " + e.getCause().getMessage());
//            }
//            throw new CannotDeleteException("Failed to delete file from S3");
//        } catch (S3Exception e) {
//            throw new CannotDeleteException("Failed to delete file from S3: " + e.getMessage());
//        }
//    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            URI uri = URI.create(fileUrl);

            String key = uri.getPath().replaceFirst("^/", "");

            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
