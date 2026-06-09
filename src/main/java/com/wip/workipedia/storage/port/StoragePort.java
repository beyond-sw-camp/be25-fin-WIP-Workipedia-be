package com.wip.workipedia.storage.port;

import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;

public interface StoragePort {
    PresignedUploadResponse createPresignedUploadUrl(PresignedUploadRequest request);
    PresignedDownloadResponse createPresignedDownloadUrl(String objectKey);
    void deleteObject(String objectKey);
}
