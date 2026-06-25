package com.wip.workipedia.aisync.worker;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.aisync.client.DocumentAiClient;
import com.wip.workipedia.aisync.client.KnowledgeAiClient;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncCleanupService;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.manual.service.ManualChangeSummaryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiSyncWorkerSummaryTest {

    @Mock AiSyncJobService aiSyncJobService;
    @Mock DocumentAiClient documentAiClient;
    @Mock TextDocumentAiClient textDocumentAiClient;
    @Mock KnowledgeAiClient knowledgeAiClient;
    @Mock AiSyncCleanupService aiSyncCleanupService;
    @Mock ManualChangeSummaryService manualChangeSummaryService;

    @Test
    void processText_routesSummaryJobToSummaryService() {
        AiSyncWorker worker = new AiSyncWorker(
            aiSyncJobService, documentAiClient, textDocumentAiClient,
            knowledgeAiClient, aiSyncCleanupService, manualChangeSummaryService);

        AiSyncJob job = AiSyncJob.create(AiSyncSourceType.MANUAL_CHANGE_SUMMARY, 42L, AiSyncOperation.UPSERT);
        ReflectionTestUtils.setField(job, "aiSyncJobId", 100L);

        when(aiSyncJobService.claimPendingTextJobs()).thenReturn(List.of(job));
        when(aiSyncJobService.isSkippable(job)).thenReturn(false);

        worker.processText();

        verify(manualChangeSummaryService).summarize(42L);
        verify(aiSyncJobService).markSynced(100L);
    }
}
