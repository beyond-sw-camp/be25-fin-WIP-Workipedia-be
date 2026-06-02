package com.wip.workipedia.faq.dto;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.repository.PopularManualProjection;
import java.time.LocalDateTime;

public record ManualSummaryResponse(
        Long manualId,
        String title,
        Long departmentId,
        long citationCount,
        LocalDateTime createdAt
) {
    public static ManualSummaryResponse from(PopularManualProjection projection) {
        return new ManualSummaryResponse(
                projection.getManualId(),
                projection.getTitle(),
                projection.getDepartmentId(),
                projection.getCitationCount() == null ? 0L : projection.getCitationCount(),
                projection.getCreatedAt()
        );
    }

    public static ManualSummaryResponse from(Manual manual) {
        return new ManualSummaryResponse(
                manual.getManualId(),
                manual.getTitle(),
                manual.getDepartmentId(),
                0L,
                manual.getCreatedAt()
        );
    }
}
