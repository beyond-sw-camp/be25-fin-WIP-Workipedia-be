package com.wip.workipedia.manual.dto;

import com.wip.workipedia.manual.domain.ManualStatus;
import jakarta.validation.constraints.Size;

public record ManualUpdateRequest(
        Long departmentId,
        @Size(max = 255)
        String title,
        String content,
        ManualStatus status,
        @Size(max = 500)
        String sourceUrl,
        @Size(max = 50)
        String version
) {
}
