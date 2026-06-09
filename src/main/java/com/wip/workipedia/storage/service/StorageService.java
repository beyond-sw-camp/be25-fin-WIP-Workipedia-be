package com.wip.workipedia.storage.service;

import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageService {

	private final StoragePort storagePort;

	public PresignedUploadResponse createPresignedUploadUrl(PresignedUploadRequest request) {
		return storagePort.createPresignedUploadUrl(request);
	}

	public PresignedDownloadResponse createPresignedDownloadUrl(String objectKey) {
		return storagePort.createPresignedDownloadUrl(objectKey);
	}

	public void deleteObject(String objectKey) {
		storagePort.deleteObject(objectKey);
	}
}
