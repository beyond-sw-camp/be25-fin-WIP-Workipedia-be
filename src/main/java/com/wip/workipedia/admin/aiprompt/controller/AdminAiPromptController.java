package com.wip.workipedia.admin.aiprompt.controller;

import com.wip.workipedia.admin.aiprompt.dto.AiPromptSettingResponse;
import com.wip.workipedia.admin.aiprompt.dto.AiPromptSettingUpdateRequest;
import com.wip.workipedia.admin.aiprompt.service.AdminAiPromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai-prompt-settings")
@RequiredArgsConstructor
public class AdminAiPromptController {

    private final AdminAiPromptService adminAiPromptService;

    @GetMapping
    public ResponseEntity<AiPromptSettingResponse> getSetting() {
        return ResponseEntity.ok(adminAiPromptService.getSetting());
    }

    @PutMapping
    public ResponseEntity<AiPromptSettingResponse> updateSetting(
            @AuthenticationPrincipal Long adminUserId,
            @Valid @RequestBody AiPromptSettingUpdateRequest request) {
        return ResponseEntity.ok(adminAiPromptService.updateSetting(adminUserId, request));
    }
}
