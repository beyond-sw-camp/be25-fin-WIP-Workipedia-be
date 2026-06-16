package com.wip.workipedia.admin.aiprompt.service;

import com.wip.workipedia.admin.aiprompt.domain.AiPromptSetting;
import com.wip.workipedia.admin.aiprompt.dto.AiPromptSettingResponse;
import com.wip.workipedia.admin.aiprompt.dto.AiPromptSettingUpdateRequest;
import com.wip.workipedia.admin.aiprompt.repository.AiPromptSettingRepository;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAiPromptService {

    private static final Long SINGLETON_ID = 1L;

    private final AiPromptSettingRepository aiPromptSettingRepository;
    private final AdminLogRepository adminLogRepository;

    @Transactional(readOnly = true)
    public AiPromptSettingResponse getSetting() {
        return AiPromptSettingResponse.from(load());
    }

    @Transactional
    public AiPromptSettingResponse updateSetting(Long adminUserId, AiPromptSettingUpdateRequest request) {
        AiPromptSetting setting = load();
        setting.update(request.customPrompt(), request.active());

        adminLogRepository.save(AdminLog.of(
                adminUserId,
                "AI_PROMPT_SETTING_UPDATE",
                "AI_PROMPT_SETTING",
                "AI 프롬프트 설정 변경",
                String.format("{\"active\":%b,\"customPromptLength\":%d}",
                        request.active(),
                        request.customPrompt() != null ? request.customPrompt().length() : 0)
        ));

        return AiPromptSettingResponse.from(setting);
    }

    // ChatbotService에서 호출 — 활성 상태인 경우에만 customPrompt 반환
    @Transactional(readOnly = true)
    public String findActiveCustomPrompt() {
        return load().getActiveCustomPrompt();
    }

    private AiPromptSetting load() {
        return aiPromptSettingRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new CustomException(ErrorType.AI_PROMPT_SETTING_NOT_FOUND));
    }
}
