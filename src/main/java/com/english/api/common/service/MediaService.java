package com.english.api.common.service;

import com.english.api.common.dto.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by hungpham on 10/5/2025
 */
public interface MediaService {
    MediaUploadResponse uploadFile(MultipartFile file, String folder) throws IOException;
    List<MediaUploadResponse> uploadFiles(List<MultipartFile> files, String folder) throws IOException;
//    void deleteFile(String key);
    void deleteFileByUrl(String avatarUrl);
}
