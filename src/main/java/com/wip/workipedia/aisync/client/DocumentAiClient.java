package com.wip.workipedia.aisync.client;

import com.wip.workipedia.aisync.client.dto.PageIngestRequest;
import com.wip.workipedia.aisync.client.dto.TextIngestRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualPage;
import com.wip.workipedia.manual.repository.ManualPageRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class DocumentAiClient {

    private static final String SOURCE_TYPE_MANUAL = "MANUAL";

    private final RestClient restClient;
    private final ManualRepository manualRepository;
    private final ManualPageRepository manualPageRepository;

    public DocumentAiClient(
        @Qualifier("syncAiRestClient") RestClient restClient,
        ManualRepository manualRepository,
        ManualPageRepository manualPageRepository
    ) {
        this.restClient = restClient;
        this.manualRepository = manualRepository;
        this.manualPageRepository = manualPageRepository;
    }

    public void ingest(Long manualId) {
        Manual manual = manualRepository.findByManualIdAndDeletedAtIsNull(manualId)
            .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));

        // manual_pages 가 있으면 파일명/페이지 메타데이터까지 AI 에 전달해 citation 에 활용한다.
        // 페이지 정보가 없는 기존 매뉴얼은 저장된 manual.content 로 ingest-text fallback 한다.
        List<ManualPage> pages = manualPageRepository
            .findByManualManualIdAndDeletedAtIsNullOrderByFileSortOrderAscPageNumberAsc(manualId);

        if (pages.isEmpty()) {
            ingestAsText(manualId, manual.getTitle(), manual.getContent());
        } else {
            ingestAsPages(manualId, manual.getTitle(), pages);
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

    private void ingestAsPages(Long manualId, String title, List<ManualPage> pages) {
        try {
            List<PageIngestRequest.Page> pagePayloads = pages.stream()
                .map(page -> new PageIngestRequest.Page(
                    page.getFileName(),
                    page.getFileKey(),
                    page.getFileSortOrder(),
                    page.getPageNumber(),
                    page.getGlobalPageNumber(),
                    page.getContent()
                ))
                .toList();
            restClient.post()
                .uri("/api/v1/documents/ingest-pages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PageIngestRequest(manualId, SOURCE_TYPE_MANUAL, title, pagePayloads))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("MANUAL 페이지 AI 인덱싱 실패: manualId={}, error={}", manualId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    private void ingestAsText(Long manualId, String title, String content) {
        try {
            restClient.post()
                .uri("/api/v1/documents/ingest-text")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TextIngestRequest(manualId, SOURCE_TYPE_MANUAL, title, content))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("MANUAL 텍스트 AI 인덱싱 실패: manualId={}, error={}", manualId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }
}
