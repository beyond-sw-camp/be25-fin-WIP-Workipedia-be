package com.wip.workipedia.aisync.worker;

import com.wip.workipedia.aisync.client.DocumentAiClient;
import com.wip.workipedia.aisync.client.KnowledgeAiClient;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.manual.service.ManualChangeSummaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSyncJobProcessorTest {

    @Mock AiSyncJobService aiSyncJobService;
    @Mock DocumentAiClient documentAiClient;
    @Mock TextDocumentAiClient textDocumentAiClient;
    @Mock KnowledgeAiClient knowledgeAiClient;
    @Mock ManualChangeSummaryService manualChangeSummaryService;

    @InjectMocks AiSyncJobProcessor processor;

    @Test
    @DisplayName("KNOWLEDGE_DATA UPSERT는 textDocumentAiClient.ingest 호출 후 markSynced")
    void processes_knowledgeUpsert() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, 1L, AiSyncOperation.UPSERT);
        ReflectionTestUtils.setField(job, "aiSyncJobId", 10L);
        when(aiSyncJobService.isSkippable(job)).thenReturn(false);

        processor.processJobs(List.of(job));

        verify(textDocumentAiClient).ingest(AiSyncSourceType.KNOWLEDGE_DATA, 1L);
        verify(aiSyncJobService).markSynced(10L);
    }

    @Test
    @DisplayName("KNOWLEDGE_DATA DELETE는 textDocumentAiClient.delete 호출 후 markSynced")
    void processes_knowledgeDelete() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, 2L, AiSyncOperation.DELETE);
        ReflectionTestUtils.setField(job, "aiSyncJobId", 11L);
        when(aiSyncJobService.isSkippable(job)).thenReturn(false);

        processor.processJobs(List.of(job));

        verify(textDocumentAiClient).delete(AiSyncSourceType.KNOWLEDGE_DATA, 2L);
        verify(aiSyncJobService).markSynced(11L);
    }

    @Test
    @DisplayName("skippable이면 AI 호출 없이 markSynced")
    void skips_whenSkippable() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, 1L, AiSyncOperation.UPSERT);
        ReflectionTestUtils.setField(job, "aiSyncJobId", 12L);
        when(aiSyncJobService.isSkippable(job)).thenReturn(true);

        processor.processJobs(List.of(job));

        verify(textDocumentAiClient, never()).ingest(any(), anyLong());
        verify(aiSyncJobService).markSynced(12L);
    }

    @Test
    @DisplayName("MANUAL_CHANGE_SUMMARY 잡은 summarize로 라우팅된다")
    void routesSummaryJobToSummaryService() {
        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.MANUAL_CHANGE_SUMMARY, 42L, AiSyncOperation.UPSERT);
        ReflectionTestUtils.setField(job, "aiSyncJobId", 100L);
        when(aiSyncJobService.isSkippable(job)).thenReturn(false);

        processor.processJobs(List.of(job));

        verify(manualChangeSummaryService).summarize(42L);
        verify(aiSyncJobService).markSynced(100L);
    }
}
