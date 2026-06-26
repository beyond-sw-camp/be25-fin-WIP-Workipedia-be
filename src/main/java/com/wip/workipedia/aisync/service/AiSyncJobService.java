package com.wip.workipedia.aisync.service;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.config.AiSyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
            log.warn("[AI-SYNC] recovering expired lease: jobId={}", job.getAiSyncJobId());
            job.resetToRetry();
        });
    }

    @Transactional
    public List<AiSyncJob> claimPendingDocumentJobs() {
        LocalDateTime now = LocalDateTime.now();
        int batchSize = aiSyncProperties.batchSize();
        logClaimCandidates(
            "document",
            now,
            batchSize,
            aiSyncJobRepository.findDocumentClaimCandidates(now, batchSize)
        );

        List<AiSyncJob> claimed = aiSyncJobRepository.claimPendingDocumentJobs(now, batchSize);
        logClaimedJobs("document", now, batchSize, claimed);
        registerAfterCommitLog("document", claimed);
        return claim(claimed);
    }

    @Transactional
    public List<AiSyncJob> claimPendingTextJobs() {
        LocalDateTime now = LocalDateTime.now();
        int batchSize = aiSyncProperties.batchSize();
        logClaimCandidates(
            "text",
            now,
            batchSize,
            aiSyncJobRepository.findTextClaimCandidates(now, batchSize)
        );

        List<AiSyncJob> claimed = aiSyncJobRepository.claimPendingTextJobs(now, batchSize);
        logClaimedJobs("text", now, batchSize, claimed);
        registerAfterCommitLog("text", claimed);
        return claim(claimed);
    }

    private List<AiSyncJob> claim(List<AiSyncJob> jobs) {
        LocalDateTime leaseExpiresAt = LocalDateTime.now().plusMinutes(aiSyncProperties.leaseMinutes());
        jobs.forEach(job -> job.startProcessing(leaseExpiresAt));
        return jobs;
    }

    private void logClaimCandidates(
        String workerType,
        LocalDateTime now,
        int batchSize,
        List<AiSyncJobRepository.AiSyncClaimCandidate> candidates
    ) {
        log.info("[AI-SYNC][CLAIM] {} candidates: count={}, batchSize={}, now={}, jobs={}",
            workerType, candidates.size(), batchSize, now, describeCandidates(candidates));
    }

    private void logClaimedJobs(
        String workerType,
        LocalDateTime now,
        int batchSize,
        List<AiSyncJob> jobs
    ) {
        log.info("[AI-SYNC][CLAIM] {} claimed: count={}, batchSize={}, now={}, jobs={}",
            workerType, jobs.size(), batchSize, now, describeJobs(jobs));
    }

    private void registerAfterCommitLog(String workerType, List<AiSyncJob> jobs) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        String jobIds = jobs.stream()
            .map(job -> String.valueOf(job.getAiSyncJobId()))
            .collect(Collectors.joining(", ", "[", "]"));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[AI-SYNC][CLAIM] {} commit completed: count={}, jobIds={}",
                    workerType, jobs.size(), jobIds);
            }
        });
    }

    private String describeCandidates(List<AiSyncJobRepository.AiSyncClaimCandidate> candidates) {
        return candidates.stream()
            .map(candidate -> "%d/%s:%d/%s/retry=%d/next=%s/lease=%s/created=%s".formatted(
                candidate.getAiSyncJobId(),
                candidate.getSourceType(),
                candidate.getSourceId(),
                candidate.getOperation(),
                candidate.getRetryCount(),
                candidate.getNextRetryAt(),
                candidate.getLeaseExpiresAt(),
                candidate.getCreatedAt()
            ))
            .collect(Collectors.joining(", ", "[", "]"));
    }

    private String describeJobs(List<AiSyncJob> jobs) {
        return jobs.stream()
            .map(job -> "%d/%s:%d/%s/retry=%d/next=%s/lease=%s/created=%s".formatted(
                job.getAiSyncJobId(),
                job.getSourceType(),
                job.getSourceId(),
                job.getOperation(),
                job.getRetryCount(),
                job.getNextRetryAt(),
                job.getLeaseExpiresAt(),
                job.getCreatedAt()
            ))
            .collect(Collectors.joining(", ", "[", "]"));
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
            log.info("[AI-SYNC] synced: jobId={}, sourceType={}, sourceId={}",
                jobId, job.getSourceType(), job.getSourceId());
        });
    }

    @Transactional
    public void markFailed(Long jobId, String error) {
        aiSyncJobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(error);
            if (job.isRetryable()) {
                log.warn("[AI-SYNC] failed (retry {}): jobId={}, error={}",
                    job.getRetryCount(), jobId, error);
            } else {
                log.error("[AI-SYNC] max retries exceeded: jobId={}, sourceType={}, sourceId={}",
                    jobId, job.getSourceType(), job.getSourceId());
            }
        });
    }
}
