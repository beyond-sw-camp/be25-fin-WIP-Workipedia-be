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
        LocalDateTime updatedAt,
        String syncStatus,
        LocalDateTime syncedAt,
        String syncError
) {
    public static ManualSummaryResponse from(Manual manual) {
        return from(manual, manual.getFileUrl() == null ? List.of() : List.of(manual.getFileUrl()));
    }

    public static ManualSummaryResponse from(Manual manual, List<String> fileUrls) {
        return from(manual, fileUrls, "EMPTY", null, null);
    }

    public static ManualSummaryResponse from(
            Manual manual,
            List<String> fileUrls,
            String syncStatus,
            LocalDateTime syncedAt,
            String syncError
    ) {
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
                manual.getUpdatedAt(),
                syncStatus,
                syncedAt,
                syncError
        );
    }
}
