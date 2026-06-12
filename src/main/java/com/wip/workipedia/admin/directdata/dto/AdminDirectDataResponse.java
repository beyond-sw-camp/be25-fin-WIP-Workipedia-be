package com.wip.workipedia.admin.directdata.dto;

import com.wip.workipedia.directdata.domain.DirectData;
import java.time.LocalDateTime;

public record AdminDirectDataResponse(
        Long directDataId,
        String title,
        String content,
        String category,
        Boolean isActive,
        Long createdBy,
        Long updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static AdminDirectDataResponse from(DirectData directData) {
        return new AdminDirectDataResponse(
                directData.getDirectDataId(),
                directData.getTitle(),
                directData.getContent(),
                directData.getCategory(),
                directData.isActive(),
                directData.getCreatedBy(),
                directData.getUpdatedBy(),
                directData.getCreatedAt(),
                directData.getUpdatedAt(),
                directData.getDeletedAt()
        );
    }
}
