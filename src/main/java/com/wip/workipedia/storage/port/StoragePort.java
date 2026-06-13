package com.wip.workipedia.storage.port;

import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.dto.StoredObjectMetadata;

public interface StoragePort {
    PresignedUploadResponse createPresignedUploadUrl(PresignedUploadRequest request);
    StoredObject upload(byte[] content, String keyPrefix, String fileName, String contentType);
    StoredObjectMetadata getObjectMetadata(String objectKey);
    PresignedDownloadResponse createPresignedDownloadUrl(String objectKey);
    void deleteObject(String objectKey);
}
