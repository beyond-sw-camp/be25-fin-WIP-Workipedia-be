package com.wip.workipedia.worki.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        Long sourceChatbotMessageId
) {
}
