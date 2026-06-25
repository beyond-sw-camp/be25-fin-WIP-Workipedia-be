package com.wip.workipedia.aisync.worker;

import com.wip.workipedia.aisync.client.DocumentAiClient;
import com.wip.workipedia.aisync.client.KnowledgeAiClient;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.CleanupTrigger;
import com.wip.workipedia.aisync.service.AiSyncCleanupService;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.manual.service.ManualChangeSummaryService;
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
    private final AiSyncCleanupService aiSyncCleanupService;
    private final ManualChangeSummaryService manualChangeSummaryService;

    // MANUAL (PDF 포함) — 파일 다운로드가 있어 느린 작업이므로 별도 주기로 처리
    @Scheduled(cron = "${ai-sync.worker.document-cron}")
    public void processDocuments() {
        log.info("[AI-SYNC] MANUAL worker started");
        aiSyncJobService.recoverExpiredLeases();
        List<AiSyncJob> jobs = aiSyncJobService.claimPendingDocumentJobs();
        log.info("[AI-SYNC] MANUAL jobs claimed: {}", jobs.size());
        processJobs(jobs);
        log.info("[AI-SYNC] MANUAL worker finished");
    }

    // 텍스트 계열 (WORKI, KNOWLEDGE_DATA, MANUAL_KNOWLEDGE, DEPT_RR)
    @Scheduled(cron = "${ai-sync.worker.text-cron}")
    public void processText() {
        log.info("[AI-SYNC] text worker started");
        aiSyncJobService.recoverExpiredLeases();
        List<AiSyncJob> jobs = aiSyncJobService.claimPendingTextJobs();
        log.info("[AI-SYNC] text jobs claimed: {}", jobs.size());
        processJobs(jobs);
        log.info("[AI-SYNC] text worker finished");
    }

    @Scheduled(cron = "${ai-sync.worker.cleanup-cron}")
    public void cleanupOldWorki() {
        log.info("[AI-SYNC][CLEANUP] 오래된 WORKI 청킹 데이터 정리 시작");
        aiSyncCleanupService.cleanupOldWorkiJobs(CleanupTrigger.SCHEDULE);
        log.info("[AI-SYNC][CLEANUP] 정리 완료");
    }

    private void processJobs(List<AiSyncJob> jobs) {
        for (AiSyncJob job : jobs) {
            long jobId = job.getAiSyncJobId();
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
