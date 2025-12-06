package com.english.api.common.controller;

import com.english.api.common.dto.MediaUploadResponse;
import com.english.api.common.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.io.IOException;
import java.util.List;

/**
 * Created by hungpham on 10/5/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder
    ) throws IOException {
        MediaUploadResponse response = mediaService.uploadFile(file, folder);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/upload/multiple",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<MediaUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("folder") String folder
    ) throws IOException {
        List<MediaUploadResponse> responses = mediaService.uploadFiles(files, folder);
        return ResponseEntity.ok(responses);
    }


    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFile(@RequestParam("filename") String filename) {
        mediaService.deleteFileByUrl(filename);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFilesInFolder(@RequestParam("folder") String folder) {
        List<String> fileUrls = mediaService.listFilesInFolder(folder);
        return ResponseEntity.ok(fileUrls);
    }
    
    @DeleteMapping("/folder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFolder(@RequestParam("folder") String folder) {
        mediaService.deleteFolder(folder);
        return ResponseEntity.ok().build();
    }

}
