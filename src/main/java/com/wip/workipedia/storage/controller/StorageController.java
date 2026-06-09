package com.wip.workipedia.storage.controller;

import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/presigned-upload")
    public ResponseEntity<PresignedUploadResponse> createPresignedUploadUrl(
        @Valid @RequestBody PresignedUploadRequest request) {
        return ResponseEntity.ok(storageService.createPresignedUploadUrl(request));
    }

    @GetMapping("/presigned-download")
    public ResponseEntity<PresignedDownloadResponse> createPresignedDownloadUrl(
        @RequestParam String objectKey) {
        return ResponseEntity.ok(storageService.createPresignedDownloadUrl(objectKey));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteObject(@RequestParam String objectKey) {
        storageService.deleteObject(objectKey);
        return ResponseEntity.noContent().build();
    }
}
