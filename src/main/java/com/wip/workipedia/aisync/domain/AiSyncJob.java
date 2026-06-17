package com.wip.workipedia.aisync.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ai_sync_jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSyncJob extends BaseTimeEntity {

    private static final int MAX_RETRY = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_sync_job_id")
    private Long aiSyncJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private AiSyncSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 10)
    private AiSyncOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiSyncStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "lease_expires_at")
    private LocalDateTime leaseExpiresAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static AiSyncJob create(AiSyncSourceType sourceType, Long sourceId, AiSyncOperation operation) {
        AiSyncJob job = new AiSyncJob();
        job.sourceType = sourceType;
        job.sourceId = sourceId;
        job.operation = operation;
        job.status = AiSyncStatus.PENDING;
        job.retryCount = 0;
        return job;
    }

    public void startProcessing(LocalDateTime leaseExpiresAt) {
        this.status = AiSyncStatus.PROCESSING;
        this.leaseExpiresAt = leaseExpiresAt;
        this.startedAt = LocalDateTime.now();
    }

    public void markSynced() {
        this.status = AiSyncStatus.SYNCED;
        this.leaseExpiresAt = null;
        this.completedAt = LocalDateTime.now();
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.retryCount++;
        this.lastError = error;
        this.leaseExpiresAt = null;
        this.completedAt = LocalDateTime.now();
        this.status = AiSyncStatus.FAILED;
        if (this.retryCount < MAX_RETRY) {
            // 지수 백오프: retryCount² 분
            this.nextRetryAt = LocalDateTime.now().plusMinutes((long) retryCount * retryCount);
        } else {
            this.nextRetryAt = null;
        }
    }

    public void resetToRetry() {
        this.status = AiSyncStatus.PENDING;
        this.leaseExpiresAt = null;
        this.startedAt = null;
    }

    public boolean isRetryable() {
        return this.retryCount < MAX_RETRY;
    }
}
