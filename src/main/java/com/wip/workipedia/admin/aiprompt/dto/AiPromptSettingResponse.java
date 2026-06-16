package com.wip.workipedia.admin.aiprompt.dto;

import com.wip.workipedia.admin.aiprompt.domain.AiPromptSetting;

public record AiPromptSettingResponse(
        String customPrompt,
        boolean active
) {
    public static AiPromptSettingResponse from(AiPromptSetting setting) {
        return new AiPromptSettingResponse(setting.getCustomPrompt(), setting.isActive());
    }
}
