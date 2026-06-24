package com.wip.workipedia.manual.dto;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.domain.ManualVersion;
import com.wip.workipedia.manual.domain.UpdateReasonLabel;
import java.time.LocalDateTime;

public record ManualVersionResponse(
        Long manualVersionId,
        Long manualId,
        Long userId,
        String manualNum,
        String updateReason,
        String updateReasonLabel,
        String title,
        String description,
        String contentDiff,
        String changeSummary,
        String summaryStatus,
        ManualStatus status,
        String sourceUrl,
        String version,
        LocalDateTime createdAt
) {
    public static ManualVersionResponse from(ManualVersion manualVersion) {
        return from(manualVersion, null);
    }

    public static ManualVersionResponse from(ManualVersion manualVersion, AiSyncJob latestSummaryJob) {
        return new ManualVersionResponse(
                manualVersion.getManualVersionId(),
                manualVersion.getManual().getManualId(),
                manualVersion.getUserId(),
                manualVersion.getManualNum(),
                manualVersion.getUpdateReason(),
                UpdateReasonLabel.toLabel(manualVersion.getUpdateReason()),
                manualVersion.getTitle(),
                manualVersion.getDescription(),
                manualVersion.getContentDiff(),
                manualVersion.getChangeSummary(),
                latestSummaryJob == null ? "NONE" : latestSummaryJob.getStatus().name(),
                manualVersion.getStatus(),
                manualVersion.getSourceUrl(),
                manualVersion.getVersion(),
                manualVersion.getCreatedAt()
        );
    }
}
