package com.wip.workipedia.aisync.service;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
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
public class AiSyncJobService {

    private final AiSyncJobRepository aiSyncJobRepository;
    private final AiSyncProperties aiSyncProperties;

    // 도메인 서비스의 @Transactional 안에서 호출 — 같은 트랜잭션으로 묶임
    @Transactional
    public void enqueue(AiSyncSourceType sourceType, Long sourceId, AiSyncOperation operation) {
        aiSyncJobRepository.save(AiSyncJob.create(sourceType, sourceId, operation));
    }

    // PROCESSING 중 lease 만료된 작업 복구
    @Transactional
    public void recoverExpiredLeases() {
        List<AiSyncJob> expired = aiSyncJobRepository.findLeaseExpiredJobs(LocalDateTime.now());
        expired.forEach(job -> {
            log.warn("lease 만료 작업 복구: jobId={}", job.getAiSyncJobId());
            job.resetToRetry();
        });
    }

    @Transactional
    public List<AiSyncJob> claimPendingDocumentJobs() {
        return claim(aiSyncJobRepository.claimPendingDocumentJobs(LocalDateTime.now(), aiSyncProperties.batchSize()));
    }

    @Transactional
    public List<AiSyncJob> claimPendingTextJobs() {
        return claim(aiSyncJobRepository.claimPendingTextJobs(LocalDateTime.now(), aiSyncProperties.batchSize()));
    }

    private List<AiSyncJob> claim(List<AiSyncJob> jobs) {
        LocalDateTime leaseExpiresAt = LocalDateTime.now().plusMinutes(aiSyncProperties.leaseMinutes());
        jobs.forEach(job -> job.startProcessing(leaseExpiresAt));
        return jobs;
    }

    // 동일 source의 더 최신 SYNCED job이 있으면 AI 호출 불필요
    @Transactional(readOnly = true)
    public boolean isSkippable(AiSyncJob job) {
        return aiSyncJobRepository.hasNewerSyncedJob(
            job.getSourceType(), job.getSourceId(), job.getCreatedAt()
        );
    }

    @Transactional
    public void markSynced(Long jobId) {
        aiSyncJobRepository.findById(jobId).ifPresent(job -> {
            job.markSynced();
            log.info("AI 동기화 완료: jobId={}, sourceType={}, sourceId={}",
                jobId, job.getSourceType(), job.getSourceId());
        });
    }

    @Transactional
    public void markFailed(Long jobId, String error) {
        aiSyncJobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(error);
            if (job.isRetryable()) {
                log.warn("AI 동기화 실패 (재시도 {}회차): jobId={}, error={}",
                    job.getRetryCount(), jobId, error);
            } else {
                log.error("AI 동기화 최대 재시도 초과: jobId={}, sourceType={}, sourceId={}",
                    jobId, job.getSourceType(), job.getSourceId());
            }
        });
    }
}
