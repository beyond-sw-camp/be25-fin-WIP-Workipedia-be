package com.wip.workipedia.admin.dto;

import com.wip.workipedia.manual.domain.ManualStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminManualCreateRequest(
        Long departmentId,
        @NotBlank
        @Size(max = 255)
        String title,
        @NotBlank
        String content,
        @NotNull
        ManualStatus status,
        @Size(max = 500)
        String sourceUrl,
        @Size(max = 50)
        String version
) {
}
