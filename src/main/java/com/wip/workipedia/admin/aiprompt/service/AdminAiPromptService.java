package com.wip.workipedia.admin.aiprompt.service;

import com.wip.workipedia.admin.aiprompt.domain.AiPromptSetting;
import com.wip.workipedia.admin.aiprompt.dto.AiPromptSettingResponse;
import com.wip.workipedia.admin.aiprompt.dto.AiPromptSettingUpdateRequest;
import com.wip.workipedia.admin.aiprompt.repository.AiPromptSettingRepository;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAiPromptService {

    private static final Long SINGLETON_ID = 1L;

    private final AiPromptSettingRepository aiPromptSettingRepository;
    private final AdminLogRepository adminLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AiPromptSettingResponse getSetting() {
        return AiPromptSettingResponse.from(load());
    }

    @Transactional
    public AiPromptSettingResponse updateSetting(Long adminUserId, AiPromptSettingUpdateRequest request) {
        AiPromptSetting setting = load();
        setting.update(request.customPrompt(), request.active());

        if (adminUserId == null || !userRepository.existsById(adminUserId)) {
            log.warn("Skip AI prompt setting admin log because actor does not exist. actorId={}", adminUserId);
            return AiPromptSettingResponse.from(setting);
        }

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
