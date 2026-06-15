package com.wip.workipedia.admin.manual.dto;

import com.wip.workipedia.manual.domain.ManualStatus;
import jakarta.validation.constraints.Size;

public record AdminManualUpdateRequest(
        Long departmentId,
        @Size(max = 255)
        String title,
        String content,
        ManualStatus status,
        @Size(max = 500)
        String sourceUrl,
        @Size(max = 500)
        String updateReason
) {
}
