package com.wip.workipedia.aisync.service;

import com.wip.workipedia.admin.aisync.dto.AiSyncCleanupResponse;
import com.wip.workipedia.aisync.client.TextDocumentAiClient;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.config.AiSyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSyncCleanupService {

    private final AiSyncJobRepository aiSyncJobRepository;
    private final TextDocumentAiClient textDocumentAiClient;
    private final AiSyncProperties aiSyncProperties;

    @Transactional
    public AiSyncCleanupResponse cleanupOldWorkiJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(aiSyncProperties.retentionDays());
        List<AiSyncJob> targets = aiSyncJobRepository.findOldSyncedWorkiLatestJobs(
            cutoff, aiSyncProperties.batchSize());

        int deleted = 0, skipped = 0, failed = 0;
        for (AiSyncJob job : targets) {
            // AI DELETE 직전 재확인 — 조회 이후 새 SYNCED 잡이 생겼으면 skip
            if (aiSyncJobRepository.hasNewerSyncedJob(
                    AiSyncSourceType.WORKI, job.getSourceId(), job.getCreatedAt())) {
                log.info("[AI-SYNC][CLEANUP] 신규 인덱싱 감지, 스킵: sourceId={}", job.getSourceId());
                skipped++;
                continue;
            }
            try {
                textDocumentAiClient.delete(AiSyncSourceType.WORKI, job.getSourceId());
                aiSyncJobRepository.softDeleteOldJobsBySourceId(
                    AiSyncSourceType.WORKI, job.getSourceId(),
                    job.getAiSyncJobId(), LocalDateTime.now());
                deleted++;
            } catch (Exception e) {
                log.warn("[AI-SYNC][CLEANUP] AI 삭제 실패, 스킵: sourceId={}, error={}",
                    job.getSourceId(), e.getMessage());
                failed++;
            }
        }
        log.info("[AI-SYNC][CLEANUP] 완료: deleted={}, skipped={}, failed={}", deleted, skipped, failed);
        return new AiSyncCleanupResponse(deleted, skipped, failed);
    }
}
