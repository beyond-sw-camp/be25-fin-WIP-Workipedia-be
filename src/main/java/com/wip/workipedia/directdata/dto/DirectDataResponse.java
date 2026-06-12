package com.wip.workipedia.directdata.dto;

import com.wip.workipedia.directdata.domain.DirectData;
import java.time.LocalDateTime;

public record DirectDataResponse(
        Long directDataId,
        String title,
        String content,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DirectDataResponse from(DirectData directData) {
        return new DirectDataResponse(
                directData.getDirectDataId(),
                directData.getTitle(),
                directData.getContent(),
                directData.getCategory(),
                directData.getCreatedAt(),
                directData.getUpdatedAt()
        );
    }
}
