package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.AiSyncStatus;

import java.time.LocalDateTime;

public record AiSyncJobResponse(
    Long aiSyncJobId,
    AiSyncSourceType sourceType,
    Long sourceId,
    AiSyncOperation operation,
    AiSyncStatus status,
    int retryCount,
    String lastError,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {
    public static AiSyncJobResponse from(AiSyncJob job) {
        return new AiSyncJobResponse(
            job.getAiSyncJobId(),
            job.getSourceType(),
            job.getSourceId(),
            job.getOperation(),
            job.getStatus(),
            job.getRetryCount(),
            job.getLastError(),
            job.getStartedAt(),
            job.getCompletedAt(),
            job.getCreatedAt()
        );
    }
}
