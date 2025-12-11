package com.english.api.common.service.impl;

import com.english.api.common.dto.MediaUploadResponse;
import com.english.api.common.exception.CannotDeleteException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            "application/pdf");

    // Chỉ các loại ảnh (dùng cho folder users)
    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "image/bmp");

    private static final Set<String> PROOF_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf");

    private static final Set<String> INVOICE_MIME_TYPES = Set.of(
            "application/pdf"
    );

    // Audio file types for speaking assessments
    private static final Set<String> AUDIO_MIME_TYPES = Set.of(
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/x-wav", "audio/mp4", "audio/webm"
    );

    // Lesson attachments - support documents, images, audio
    private static final Set<String> LESSON_ATTACHMENT_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "image/bmp",
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/x-wav", "audio/mp4", "audio/webm",
            "application/pdf",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    private static final Map<String, FolderRule> FOLDER_RULES = Map.of(
            "users", new FolderRule(2 * 1024 * 1024, IMAGE_MIME_TYPES), // 2MB, chỉ ảnh
            "certificate_proofs", new FolderRule(5 * 1024 * 1024, PROOF_MIME_TYPES),
            "forums", new FolderRule(50 * 1024 * 1024, ALLOWED_MIME_TYPES), // 50MB, tất cả loại hợp lệ
            "course_thumbnail", new FolderRule(2 * 1024 * 1024, IMAGE_MIME_TYPES) ,// 2MB, chỉ ảnh
            "lesson_attachments", new FolderRule(20 * 1024 * 1024, LESSON_ATTACHMENT_MIME_TYPES), // 20MB, documents + media
            "invoices", new FolderRule(2 * 1024 * 1024, INVOICE_MIME_TYPES),
            "speaking_assessments", new FolderRule(20 * 1024 * 1024, AUDIO_MIME_TYPES), // 20MB, audio only
            "quiz", new FolderRule(
                10 * 1024 * 1024, // 10MB
                new HashSet<>() {{
                    addAll(IMAGE_MIME_TYPES);
                    addAll(AUDIO_MIME_TYPES);
                }}
            )
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
                .orElseThrow(() -> new ResourceInvalidException("Unknown or unauthorized upload folder: " + folder));

        // Kiểm tra loại MIME
        if (!rule.allowedTypes.contains(contentType)) {
            throw new ResourceInvalidException(String.format(
                    "File type '%s' is not allowed in folder '%s'.", contentType, folder));
        }

        // Kiểm tra dung lượng tối đa
        if (file.getSize() > rule.maxSize) {
            throw new ResourceInvalidException(String.format(
                    "File size exceeds the limit for folder '%s' (%.2f MB max).",
                    folder, rule.maxSize / 1024.0 / 1024.0));
        }

        // Kiểm tra phần mở rộng file
        if (!originalFilename.matches("(?i).+\\.(png|jpe?g|gif|webp|bmp|mp3|wav|ogg|mp4|webm|pdf|docx?|pptx?|xlsx?|txt)$")) {
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
                filename);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        CompletableFuture<PutObjectResponse> future = s3Client.putObject(req,
                AsyncRequestBody.fromBytes(file.getBytes()));

        future.join();

        String url = String.join("/", publicUrl.replaceAll("/+$", ""), key);

        return new MediaUploadResponse(
                filename,
                url,
                file.getSize(),
                contentType);
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

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank())
            return;
        try {
            URI uri = URI.create(fileUrl);

            String key = uri.getPath().replaceFirst("^/", "");

            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteReq);
        } catch (Exception e) {
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            // Bước 1: List tất cả versions của file
            ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                    .bucket(bucket)
                    .prefix(key)
                    .build();

            CompletableFuture<ListObjectVersionsResponse> listFuture = s3Client.listObjectVersions(listRequest);
            ListObjectVersionsResponse listResponse = listFuture.join();

            log.info("Found " + listResponse.versions().size() + " versions");
            log.info("Found " + listResponse.deleteMarkers().size() + " delete markers");

            // Bước 2: Xóa tất cả versions
            List<CompletableFuture<DeleteObjectResponse>> deleteFutures = new ArrayList<>();

            // Xóa các versions thực tế
            for (ObjectVersion version : listResponse.versions()) {
                if (version.key().equals(key)) {
                    log.info("Deleting version: " + version.versionId());

                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .versionId(version.versionId())
                            .build();

                    deleteFutures.add(s3Client.deleteObject(deleteRequest));
                }
            }

            // Xóa các delete markers
            for (DeleteMarkerEntry marker : listResponse.deleteMarkers()) {
                if (marker.key().equals(key)) {
                    log.info("Deleting marker: " + marker.versionId());

                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .versionId(marker.versionId())
                            .build();

                    deleteFutures.add(s3Client.deleteObject(deleteRequest));
                }
            }

            // Chờ tất cả delete hoàn thành
            CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0])).join();

            log.info("✓ Successfully deleted all versions and markers for: " + key);

        } catch (CompletionException e) {
            log.error("CompletionException: " + e.getMessage());
            if (e.getCause() instanceof S3Exception) {
                S3Exception s3Ex = (S3Exception) e.getCause();
                log.error("S3 Error Code: " + s3Ex.awsErrorDetails().errorCode());
                log.error("S3 Error Message: " + s3Ex.awsErrorDetails().errorMessage());
                throw new CannotDeleteException(
                        "Failed to delete file from S3: " + s3Ex.awsErrorDetails().errorMessage());
            }
            throw new CannotDeleteException("Failed to delete file from S3: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            throw new CannotDeleteException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public List<String> listFilesInFolder(String folderPath) {
        log.info("=== LIST FILES IN FOLDER ===");
        log.info("Folder path: " + folderPath);
        
        // Chuẩn hóa folder path
        String prefix = folderPath.replaceAll("^/+", "").replaceAll("/+$", "") + "/";
        log.info("Normalized prefix: " + prefix);
        
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .maxKeys(1000) // Giới hạn 1000 files, có thể tăng hoặc phân trang
                    .build();
            
            CompletableFuture<ListObjectsV2Response> future = s3Client.listObjectsV2(listRequest);
            ListObjectsV2Response response = future.join();
            
            List<String> fileUrls = response.contents().stream()
                    .filter(s3Object -> !s3Object.key().endsWith("/")) // Lọc bỏ folder rỗng
                    .map(s3Object -> {
                        String url = String.join("/", publicUrl.replaceAll("/+$", ""), s3Object.key());
                        log.info("Found file: " + s3Object.key() + " -> " + url);
                        return url;
                    })
                    .collect(Collectors.toList());
            
            log.info("Total files found: " + fileUrls.size());
            return fileUrls;
            
        } catch (CompletionException e) {
            log.error("Failed to list files: " + e.getMessage());
            if (e.getCause() instanceof S3Exception) {
                S3Exception s3Ex = (S3Exception) e.getCause();
                throw new ResourceInvalidException("Failed to list files: " + s3Ex.awsErrorDetails().errorMessage());
            }
            throw new ResourceInvalidException("Failed to list files in folder");
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            throw new ResourceInvalidException("Failed to list files in folder");
        }
    }

    @Override
    public void deleteFolder(String folderPath) {
        log.info("=== DELETE FOLDER ===");
        log.info("Folder path: " + folderPath);
        
        // Chuẩn hóa folder path
        String prefix = folderPath.replaceAll("^/+", "").replaceAll("/+$", "") + "/";
        log.info("Normalized prefix: " + prefix);
        
        try {
            // Bước 1: List tất cả files và versions trong folder
            ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .maxKeys(1000)
                    .build();
            
            CompletableFuture<ListObjectVersionsResponse> listFuture = s3Client.listObjectVersions(listRequest);
            ListObjectVersionsResponse listResponse = listFuture.join();
            
            log.info("Found " + listResponse.versions().size() + " file versions");
            log.info("Found " + listResponse.deleteMarkers().size() + " delete markers");
            
            if (listResponse.versions().isEmpty() && listResponse.deleteMarkers().isEmpty()) {
                log.info("Folder is empty or does not exist");
                return;
            }
            
            // Bước 2: Xóa tất cả versions và delete markers
            List<CompletableFuture<DeleteObjectResponse>> deleteFutures = new ArrayList<>();
            
            // Xóa các file versions
            for (ObjectVersion version : listResponse.versions()) {
                log.info("Deleting: " + version.key() + " (version: " + version.versionId() + ")");
                
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(version.key())
                        .versionId(version.versionId())
                        .build();
                
                deleteFutures.add(s3Client.deleteObject(deleteRequest));
            }
            
            // Xóa các delete markers
            for (DeleteMarkerEntry marker : listResponse.deleteMarkers()) {
                log.info("Deleting marker: " + marker.key() + " (version: " + marker.versionId() + ")");
                
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(marker.key())
                        .versionId(marker.versionId())
                        .build();
                
                deleteFutures.add(s3Client.deleteObject(deleteRequest));
            }
            
            // Chờ tất cả delete hoàn thành
            CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0])).join();
            
            log.info("✓ Successfully deleted folder and all contents: " + folderPath);
            log.info("Total items deleted: " + deleteFutures.size());
            
        } catch (CompletionException e) {
            log.error("Failed to delete folder: " + e.getMessage());
            if (e.getCause() instanceof S3Exception) {
                S3Exception s3Ex = (S3Exception) e.getCause();
                throw new CannotDeleteException("Failed to delete folder: " + s3Ex.awsErrorDetails().errorMessage());
            }
            throw new CannotDeleteException("Failed to delete folder");
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            throw new CannotDeleteException("Failed to delete folder: " + e.getMessage());
        }
    }


}
