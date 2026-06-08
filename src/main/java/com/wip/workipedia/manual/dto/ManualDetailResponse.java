package com.wip.workipedia.manual.dto;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import java.time.LocalDateTime;

public record ManualDetailResponse(
        Long manualId,
        Long departmentId,
        String title,
        String content,
        ManualStatus status,
        String sourceUrl,
        String version,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ManualDetailResponse from(Manual manual) {
        return new ManualDetailResponse(
                manual.getManualId(),
                manual.getDepartmentId(),
                manual.getTitle(),
                manual.getContent(),
                manual.getStatus(),
                manual.getSourceUrl(),
                manual.getVersion(),
                manual.getCreatedBy(),
                manual.getCreatedAt(),
                manual.getUpdatedAt()
        );
    }
}
