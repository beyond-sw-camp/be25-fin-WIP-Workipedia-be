package com.wip.workipedia.manual.dto;

import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.domain.ManualVersion;
import java.time.LocalDateTime;

public record ManualVersionResponse(
        Long manualVersionId,
        Long manualId,
        Long userId,
        String manualNum,
        String updateReason,
        String title,
        String description,
        String content,
        String contentDiff,
        ManualStatus status,
        String sourceUrl,
        String version,
        LocalDateTime createdAt
) {
    public static ManualVersionResponse from(ManualVersion manualVersion) {
        return new ManualVersionResponse(
                manualVersion.getManualVersionId(),
                manualVersion.getManual().getManualId(),
                manualVersion.getUserId(),
                manualVersion.getManualNum(),
                manualVersion.getUpdateReason(),
                manualVersion.getTitle(),
                manualVersion.getDescription(),
                manualVersion.getContent(),
                manualVersion.getContentDiff(),
                manualVersion.getStatus(),
                manualVersion.getSourceUrl(),
                manualVersion.getVersion(),
                manualVersion.getCreatedAt()
        );
    }
}
