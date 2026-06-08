package com.wip.workipedia.flashchat.dto;

import java.util.List;

public record FlashChatPolicyResponse(
        int messageTtlSeconds,
        int sendCooldownSeconds,
        List<String> bannedWords
) {}
