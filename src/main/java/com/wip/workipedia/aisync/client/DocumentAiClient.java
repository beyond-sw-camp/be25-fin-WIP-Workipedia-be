package com.wip.workipedia.aisync.client;

import com.wip.workipedia.aisync.client.dto.TextIngestRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualFile;
import com.wip.workipedia.manual.repository.ManualFileRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@Component
public class DocumentAiClient {

    private final RestClient restClient;
    private final ManualRepository manualRepository;
    private final ManualFileRepository manualFileRepository;

    public DocumentAiClient(
        @Qualifier("syncAiRestClient") RestClient restClient,
        ManualRepository manualRepository,
        ManualFileRepository manualFileRepository
    ) {
        this.restClient = restClient;
        this.manualRepository = manualRepository;
        this.manualFileRepository = manualFileRepository;
    }

    public void ingest(Long manualId) {
        Manual manual = manualRepository.findByManualIdAndDeletedAtIsNull(manualId)
            .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));

        List<ManualFile> files = manualFileRepository
            .findByManualManualIdAndDeletedAtIsNullOrderBySortOrderAsc(manualId);

        if (files.isEmpty()) {
            ingestAsText(manualId, manual.getTitle(), manual.getContent());
        } else {
            ingestFiles(manualId, manual.getTitle(), files);
        }
    }

    public void delete(Long manualId) {
        try {
            restClient.delete()
                .uri("/api/v1/documents/{sourceId}?source_type=MANUAL", manualId)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("MANUAL AI 삭제 실패: manualId={}, error={}", manualId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    private void ingestFiles(Long manualId, String title, List<ManualFile> files) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("source_id", manualId.toString());
            body.add("source_type", "MANUAL");
            body.add("title", title);
            for (ManualFile mf : files) {
                byte[] bytes = downloadBytes(mf.getFileUrl());
                String fileName = mf.getFileKey().substring(mf.getFileKey().lastIndexOf('/') + 1);
                body.add("files", new ByteArrayResource(bytes) {
                    @Override public String getFilename() { return fileName; }
                });
            }
            restClient.post()
                .uri("/api/v1/documents/ingest")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toBodilessEntity();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("MANUAL AI 파일 인덱싱 실패: manualId={}, error={}", manualId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    private void ingestAsText(Long manualId, String title, String content) {
        try {
            restClient.post()
                .uri("/api/v1/documents/ingest-text")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TextIngestRequest(manualId, "MANUAL", title, content))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("MANUAL 텍스트 AI 인덱싱 실패: manualId={}, error={}", manualId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    private byte[] downloadBytes(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            return client.send(req, HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }
}
