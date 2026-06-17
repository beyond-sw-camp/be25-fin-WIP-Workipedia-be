package com.wip.workipedia.aisync.worker;

import com.wip.workipedia.aisync.client.DocumentAiClient;
import com.wip.workipedia.aisync.client.KnowledgeAiClient;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSyncWorker {

    private final AiSyncJobService aiSyncJobService;
    private final DocumentAiClient documentAiClient;
    private final TextDocumentAiClient textDocumentAiClient;
    private final KnowledgeAiClient knowledgeAiClient;

    @Scheduled(fixedDelayString = "${ai-sync.worker.fixed-delay-ms}")
    public void process() {
        aiSyncJobService.recoverExpiredLeases();

        List<AiSyncJob> jobs = aiSyncJobService.claimPendingJobs();
        for (AiSyncJob job : jobs) {
            long jobId = job.getAiSyncJobId();
            try {
                // 더 최신 SYNCED job이 있으면 AI 호출 없이 스킵
                if (aiSyncJobService.isSkippable(job)) {
                    log.info("최신 job 존재로 스킵: jobId={}, sourceType={}, sourceId={}",
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
        }
    }
}
