package com.wip.workipedia.manual.dto;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import java.time.LocalDateTime;

public record ManualSummaryResponse(
        Long manualId,
        Long departmentId,
        String title,
        ManualStatus status,
        String sourceUrl,
        String version,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ManualSummaryResponse from(Manual manual) {
        return new ManualSummaryResponse(
                manual.getManualId(),
                manual.getDepartmentId(),
                manual.getTitle(),
                manual.getStatus(),
                manual.getSourceUrl(),
                manual.getVersion(),
                manual.getCreatedBy(),
                manual.getCreatedAt(),
                manual.getUpdatedAt()
        );
    }
}
