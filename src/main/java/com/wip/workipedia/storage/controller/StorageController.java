package com.wip.workipedia.storage.controller;

import com.wip.workipedia.common.response.ApiResponse;
import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
	public ResponseEntity<ApiResponse<PresignedUploadResponse>> createPresignedUploadUrl(
		@Valid @RequestBody PresignedUploadRequest request) {
		return ApiResponse.success(HttpStatus.OK, "Presigned upload URL 발급 완료",
			storageService.createPresignedUploadUrl(request));
	}

	@GetMapping("/presigned-download")
	public ResponseEntity<ApiResponse<PresignedDownloadResponse>> createPresignedDownloadUrl(
		@RequestParam String objectKey) {
		return ApiResponse.success(HttpStatus.OK, "Presigned download URL 발급 완료",
			storageService.createPresignedDownloadUrl(objectKey));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> deleteObject(@RequestParam String objectKey) {
		storageService.deleteObject(objectKey);
		return ApiResponse.success(HttpStatus.OK, "파일 삭제 완료");
	}
}
