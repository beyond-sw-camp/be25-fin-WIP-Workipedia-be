package com.wip.workipedia.admin.directdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminDirectDataRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String content,

        @Size(max = 100)
        String category,

        Boolean isActive
) {
}
