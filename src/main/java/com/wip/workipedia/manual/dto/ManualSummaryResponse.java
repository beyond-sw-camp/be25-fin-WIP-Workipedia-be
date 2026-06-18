package com.wip.workipedia.manual.dto;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ManualSummaryResponse(
        Long manualId,
        Long departmentId,
        String title,
        String description,
        ManualStatus status,
        String sourceUrl,
        String fileUrl,
        List<String> fileUrls,
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
                manual.getDescription(),
                manual.getStatus(),
                manual.getSourceUrl(),
                manual.getFileUrl(),
                manual.getFileUrl() == null ? List.of() : List.of(manual.getFileUrl()),
                manual.getVersion(),
                manual.getCreatedBy(),
                manual.getCreatedAt(),
                manual.getUpdatedAt()
        );
    }

    public static ManualSummaryResponse from(Manual manual, List<String> fileUrls) {
        return new ManualSummaryResponse(
                manual.getManualId(),
                manual.getDepartmentId(),
                manual.getTitle(),
                manual.getDescription(),
                manual.getStatus(),
                manual.getSourceUrl(),
                fileUrls.isEmpty() ? manual.getFileUrl() : fileUrls.get(0),
                fileUrls,
                manual.getVersion(),
                manual.getCreatedBy(),
                manual.getCreatedAt(),
                manual.getUpdatedAt()
        );
    }
}
