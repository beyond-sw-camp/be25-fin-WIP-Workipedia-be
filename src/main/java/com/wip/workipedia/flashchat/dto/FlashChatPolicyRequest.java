package com.wip.workipedia.flashchat.dto;

import jakarta.validation.constraints.Min;
import java.util.List;

public record FlashChatPolicyRequest(
        @Min(60) int messageTtlSeconds,
        @Min(0) int sendCooldownSeconds,
        List<String> bannedWords
) {}
