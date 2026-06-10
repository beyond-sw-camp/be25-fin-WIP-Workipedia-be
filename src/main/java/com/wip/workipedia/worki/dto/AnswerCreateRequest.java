package com.wip.workipedia.worki.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerCreateRequest(
        @NotBlank String content
) {
}
