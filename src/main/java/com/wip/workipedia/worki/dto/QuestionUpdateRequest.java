package com.wip.workipedia.worki.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionUpdateRequest(
        @NotBlank String title,
        @NotBlank String content
) {
}
