package com.wip.workipedia.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotMessageRequest(
        @NotBlank
        @Size(min = 1, max = 2000)
        String question
) {}
