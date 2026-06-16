package com.wip.workipedia.admin.aiprompt.dto;

import jakarta.validation.constraints.Size;

public record AiPromptSettingUpdateRequest(
        @Size(min = 1, max = 4000, message = "custom_prompt는 1자 이상 4000자 이하여야 합니다.")
        String customPrompt,
        boolean active
) {}
