package com.wip.workipedia.storage.service;

import com.wip.workipedia.storage.dto.PresignedDownloadResponse;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.port.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    StoragePort storagePort;

    @InjectMocks
    StorageService storageService;

    @Test
    void createPresignedUploadUrl_delegates_to_port() {
        PresignedUploadRequest req = new PresignedUploadRequest("photo.jpg", "image/jpeg");
        PresignedUploadResponse expected = new PresignedUploadResponse(
            "https://upload.url", "tickets/replies/uuid/photo.jpg", "https://public.url/photo.jpg");
        given(storagePort.createPresignedUploadUrl(req)).willReturn(expected);

        PresignedUploadResponse result = storageService.createPresignedUploadUrl(req);

        assertThat(result).isEqualTo(expected);
        verify(storagePort).createPresignedUploadUrl(req);
    }

    @Test
    void createPresignedDownloadUrl_delegates_to_port() {
        String objectKey = "tickets/replies/uuid/photo.jpg";
        PresignedDownloadResponse expected = new PresignedDownloadResponse("https://download.url");
        given(storagePort.createPresignedDownloadUrl(objectKey)).willReturn(expected);

        PresignedDownloadResponse result = storageService.createPresignedDownloadUrl(objectKey);

        assertThat(result).isEqualTo(expected);
        verify(storagePort).createPresignedDownloadUrl(objectKey);
    }

    @Test
    void upload_delegates_to_port() {
        byte[] content = "pdf-content".getBytes();
        String keyPrefix = "manuals";
        String fileName = "guide.pdf";
        String contentType = "application/pdf";
        StoredObject expected = new StoredObject(
            "manuals/uuid/guide.pdf",
            "https://public.url/manuals/uuid/guide.pdf");
        given(storagePort.upload(content, keyPrefix, fileName, contentType)).willReturn(expected);

        StoredObject result = storageService.upload(content, keyPrefix, fileName, contentType);

        assertThat(result).isEqualTo(expected);
        verify(storagePort).upload(content, keyPrefix, fileName, contentType);
    }

    @Test
    void deleteObject_delegates_to_port() {
        String objectKey = "tickets/replies/uuid/photo.jpg";

        storageService.deleteObject(objectKey);

        verify(storagePort).deleteObject(objectKey);
    }
}
