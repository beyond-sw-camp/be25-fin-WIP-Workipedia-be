package com.wip.workipedia.manual.dto;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ManualDetailResponse(
        Long manualId,
        Long departmentId,
        String title,
        String description,
        String content,
        ManualStatus status,
        String sourceUrl,
        String fileUrl,
        List<String> fileUrls,
        String version,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ManualDetailResponse from(Manual manual) {
        return from(manual, manual.getFileUrl() == null ? List.of() : List.of(manual.getFileUrl()));
    }

    public static ManualDetailResponse from(Manual manual, List<String> fileUrls) {
        return new ManualDetailResponse(
                manual.getManualId(),
                manual.getDepartmentId(),
                manual.getTitle(),
                manual.getDescription(),
                manual.getContent(),
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
