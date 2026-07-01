package com.wip.workipedia.aisync.worker;

import com.wip.workipedia.aisync.client.DocumentAiClient;
import com.wip.workipedia.aisync.client.KnowledgeAiClient;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.manual.service.ManualChangeSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ai_sync_jobs 잡 리스트를 실제로 처리하는 공유 컴포넌트.
 * 스케줄 워커(AiSyncWorker)와 즉시 실행 드레인(AiSyncKnowledgeRunner)이 함께 사용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiSyncJobProcessor {

    private final AiSyncJobService aiSyncJobService;
    private final DocumentAiClient documentAiClient;
    private final TextDocumentAiClient textDocumentAiClient;
    private final KnowledgeAiClient knowledgeAiClient;
    private final ManualChangeSummaryService manualChangeSummaryService;

    public void processJobs(List<AiSyncJob> jobs) {
        for (AiSyncJob job : jobs) {
            Long jobId = job.getAiSyncJobId();
            try {
                if (aiSyncJobService.isSkippable(job)) {
                    log.info("[AI-SYNC] skipped (newer job exists): jobId={}, sourceType={}, sourceId={}",
                        jobId, job.getSourceType(), job.getSourceId());
                    aiSyncJobService.markSynced(jobId);
                    continue;
                }
                execute(job);
                aiSyncJobService.markSynced(jobId);
            } catch (Exception e) {
                aiSyncJobService.markFailed(jobId, e.getMessage());
            }
        }
    }

    private void execute(AiSyncJob job) {
        boolean isUpsert = job.getOperation() == AiSyncOperation.UPSERT;
        switch (job.getSourceType()) {
            case MANUAL -> {
                if (isUpsert) documentAiClient.ingest(job.getSourceId());
                else documentAiClient.delete(job.getSourceId());
            }
            case WORKI, KNOWLEDGE_DATA, MANUAL_KNOWLEDGE -> {
                if (isUpsert) textDocumentAiClient.ingest(job.getSourceType(), job.getSourceId());
                else textDocumentAiClient.delete(job.getSourceType(), job.getSourceId());
            }
            case DEPT_RR -> {
                if (isUpsert) knowledgeAiClient.sync(job.getSourceId());
                else knowledgeAiClient.delete(job.getSourceId());
            }
            case MANUAL_CHANGE_SUMMARY -> manualChangeSummaryService.summarize(job.getSourceId());
        }
    }
}
