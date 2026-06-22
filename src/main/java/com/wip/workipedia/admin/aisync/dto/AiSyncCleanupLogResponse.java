package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncCleanupLog;

import java.time.LocalDateTime;

public record AiSyncCleanupLogResponse(
    String triggeredBy,
    int deletedCount,
    int skippedCount,
    int failedCount,
    LocalDateTime completedAt
) {
    public static AiSyncCleanupLogResponse from(AiSyncCleanupLog log) {
        return new AiSyncCleanupLogResponse(
            log.getTriggeredBy().name(),
            log.getDeletedCount(),
            log.getSkippedCount(),
            log.getFailedCount(),
            log.getCompletedAt()
        );
    }
}
